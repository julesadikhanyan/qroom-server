package com.qroom.room.impl.service;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.qroom.common.booking.config.BookingProperties;
import com.qroom.notification.api.messaging.BookingChangedEvent;
import com.qroom.notification.api.messaging.BookingCreatedEvent;
import com.qroom.notification.api.messaging.UserStatusChangedEvent;
import com.qroom.notification.impl.service.NotificationService;
import com.qroom.room.api.model.BookingDTO;
import com.qroom.user.api.model.HistoryDTO;
import com.qroom.room.api.model.RoomDTO;
import com.qroom.room.impl.entity.*;
import com.qroom.room.impl.mapper.HistoryMapper;
import com.qroom.room.impl.mapper.RoomMapper;
import com.qroom.room.impl.repository.*;
import com.qroom.user.impl.entity.User;
import com.qroom.user.impl.mapper.UserMapper;
import com.qroom.user.impl.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("UnstableApiUsage")
public class RoomService implements com.qroom.room.api.service.RoomService {

    private final RoomRepository roomRepository;
    private final HistoryRepository historyRepository;
    private final HistoryUserRepository historyUserRepository;
    private final HistoryStatusRepository historyStatusRepository;
    private final HistoryUserStatusRepository historyUserStatusRepository;
    private final UserRepository userRepository;
    private final EventBus eventBus = new EventBus();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    private final BookingProperties bookingProperties;

    public RoomService(RoomRepository roomRepository,
                       HistoryRepository historyRepository,
                       HistoryUserRepository historyUserRepository,
                       HistoryStatusRepository historyStatusRepository,
                       HistoryUserStatusRepository historyUserStatusRepository,
                       UserRepository userRepository,
                       NotificationService notificationService,
                       BookingProperties bookingProperties) {
        this.roomRepository = roomRepository;
        this.historyRepository = historyRepository;
        this.historyUserRepository = historyUserRepository;
        this.historyStatusRepository = historyStatusRepository;
        this.historyUserStatusRepository = historyUserStatusRepository;
        this.userRepository = userRepository;
        this.bookingProperties = bookingProperties;
        eventBus.register(notificationService);
        eventBus.register(this);
    }

    @Override
    public List<RoomDTO> getAllRooms() {
        List<Room> roomList = roomRepository.findAll();
        List<RoomDTO> roomDTOList = new ArrayList<>();

        roomList.forEach(
                room -> {
                    Boolean isFree = getConflictTime(
                            new ArrayList<>(room.getHistories()),
                            OffsetDateTime.now(),
                            OffsetDateTime.now().plusMinutes(30)
                    ).isEmpty();
                    roomDTOList.add(RoomMapper.roomToDTO(room, isFree));
                }
        );
        return roomDTOList;
    }

    @Override
    public RoomDTO getRoom(UUID id) {
        Room room = getOrThrowRoom(id);
        Boolean isFree = getConflictTime(
                new ArrayList<>(room.getHistories()),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(15)
        ).isEmpty();
        return RoomMapper.roomToDTO(room, isFree);
    }

    @Override
    public List<HistoryDTO> getRoomBookings(UUID id, Date date) {
        Room room = getOrThrowRoom(id);
        List<History> historyList = new ArrayList<>(room.getHistories());
        historyList = historyList.stream()
                .filter(history ->
                        !history.getStatus().getName().equals(HistoryStatusRepository.StatusNames.CANCELED.name())
                )
                .collect(Collectors.toList());
        if (date != null) {
            historyList = historyList.stream()
                    .filter(history ->
                            (history.getStartTime().getYear() == date.getYear() + 1900) &
                                    (history.getStartTime().getMonthValue() == date.getMonth() + 1) &
                                    (history.getStartTime().getDayOfMonth() == date.getDate())
                    )
                    .collect(Collectors.toList());
        }
        return historyList.stream()
                .map(HistoryMapper::historyToHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void bookRoom(BookingDTO bookingDTO, UUID adminUuid) {

        User adminUser = getOrThrowUser(adminUuid);
        if (bookingDTO.getTime().getStartTime().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException(
                    "Booking start time is present - " + bookingDTO.getTime().getStartTime().toString()
            );
        }
        if (bookingDTO.getTime().getStartTime().isAfter(bookingDTO.getTime().getEndTime())) {
            throw new IllegalArgumentException(
                    "Booking start time is after booking end time"
            );
        }
        if (bookingDTO.getTime().getStartTime().getDayOfYear() != bookingDTO.getTime().getEndTime().getDayOfYear()) {
            throw new IllegalArgumentException(
                    "Booking start and end time not in same days"
            );
        }
        if (bookingDTO.getTime().getStartTime().equals(bookingDTO.getTime().getEndTime())) {
            throw new IllegalArgumentException("Booking start and end time are equal");
        }

        Duration distance = Duration.between(
                bookingDTO.getTime().getStartTime(),
                bookingDTO.getTime().getEndTime()
        );

        if (!distance.abs().minus(bookingProperties.getMaxMeetingDuration()).isNegative()) {
            throw new IllegalArgumentException(
                    "Meeting is to long, maximum is " + bookingProperties.getMaxMeetingDuration().toString()
            );
        }

        Duration maxDuration = Duration.between(OffsetDateTime.now(), bookingDTO.getTime().getStartTime());
        if (!maxDuration.abs().minus(bookingProperties.getMaxFutureDistance()).isNegative()) {
            throw new IllegalArgumentException("You can`t book room more than a year in advance");
        }

        Room room = getOrThrowRoom(bookingDTO.getRoomUuid());
        List<History> conflictHistoryList = getConflictTime(
                new ArrayList<>(room.getHistories()),
                bookingDTO.getTime().getStartTime(),
                bookingDTO.getTime().getEndTime()
        );

        if (!conflictHistoryList.isEmpty()) {
            throw new IllegalArgumentException("Booking time conflicts with already booked time segments");
        }

        History history = new History();
        history.setRoom(getOrThrowRoom(bookingDTO.getRoomUuid()));
        history.setTitle(bookingDTO.getTitle());
        history.setStartTime(bookingDTO.getTime().getStartTime());
        history.setEndTime(bookingDTO.getTime().getEndTime());
        history.setStatus(getHistoryStatusByName(HistoryStatusRepository.StatusNames.BOOKED.name()));
        historyRepository.save(history);

        HistoryUser historyUser = new HistoryUser();
        historyUser.setHistoryUuid(history.getUuid());
        historyUser.setUserUuid(adminUuid);
        historyUser.setIsAdmin(true);
        historyUser.setStatus(getHistoryUserStatusByName(HistoryUserStatusRepository.StatusNames.CONFIRMED.name()));
        historyUserRepository.save(historyUser);

        List<UUID> failedUsersUuids = new ArrayList<>();

        bookingDTO.getInvitedUsers()
                .stream()
                .filter(userUuid -> !userUuid.equals(adminUuid))
                .forEach(user -> {
                    try {
                        User invitedUser = getOrThrowUser(user);
                        HistoryUser tempHistoryUser = new HistoryUser();
                        tempHistoryUser.setHistoryUuid(history.getUuid());
                        tempHistoryUser.setUserUuid(user);
                        tempHistoryUser.setIsAdmin(false);
                        tempHistoryUser.setStatus(getHistoryUserStatusByName(HistoryUserStatusRepository.StatusNames.PENDING.name()));
                        historyUserRepository.save(tempHistoryUser);
                        eventBus.post(
                                new BookingCreatedEvent(HistoryMapper.historyToHistoryDTO(history), UserMapper.userToUserDTO(invitedUser))
                        );
                    }
                    catch (NoSuchElementException exception){
                        failedUsersUuids.add(user);
                    }
                });

        eventBus.post(
                new BookingCreatedEvent(HistoryMapper.historyToHistoryDTO(history), UserMapper.userToUserDTO(adminUser))
        );

        executorService.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            endOfBooking(history.getUuid());
                        }
                        catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                },
                Duration.between(OffsetDateTime.now(), history.getEndTime()).toSeconds(),
                TimeUnit.SECONDS
        );
    }

    private void endOfBooking(UUID historyUuid) {
        History history = getOrThrowHistory(historyUuid);
        if (history.getStatus().getName().equals(HistoryStatusRepository.StatusNames.BOOKED.name())) {
            eventBus.post(historyUuid);
        }
    }

    @Subscribe
    public void handlePassHistoryEvent(UUID historyUuid) {
        History history = getOrThrowHistory(historyUuid);
        history.setStatus(getHistoryStatusByName(HistoryStatusRepository.StatusNames.PASSED.name()));
        historyRepository.save(history);
    }

    @Override
    public void deleteRoomBooking(UUID historyUuid, UUID userUuid) {
        // TODO: filter statuses and throw exceptions

        History history = getOrThrowHistory(historyUuid);
        List<HistoryUser> userHistoryList =  history.getHistoryUsers()
                            .stream()
                            .filter(historyUser -> historyUser.getUserUuid().equals(userUuid))
                            .collect(Collectors.toList());

        if (userHistoryList.isEmpty()) {
            throw new NoSuchElementException("No such user in invited users");
        }
        HistoryUser historyUser = userHistoryList.get(0);
        HistoryStatus oldHistoryStatus = history.getStatus();
        if (historyUser.getIsAdmin()){
            history.setStatus(getHistoryStatusByName(HistoryStatusRepository.StatusNames.CANCELED.name()));
            historyRepository.save(history);
            history.getHistoryUsers()
                    .forEach(currentHistoryUser -> {
                        eventBus.post(new BookingChangedEvent(
                                HistoryMapper.historyToHistoryDTO(history),
                                UserMapper.userToUserDTO(historyUser.getUser()),
                                oldHistoryStatus.getName(),
                                history.getStatus().getName()
                            )
                        );
                    });
        }
        else {
            HistoryUserStatus oldUserStatus = historyUser.getStatus();
            historyUser.setStatus(getHistoryUserStatusByName(HistoryUserStatusRepository.StatusNames.REJECTED.name()));
            historyUserRepository.save(historyUser);
            eventBus.post(new UserStatusChangedEvent(
                    HistoryMapper.historyToHistoryDTO(history),
                    UserMapper.userToUserDTO(historyUser.getUser()),
                    oldUserStatus.getName(),
                    historyUser.getStatus().getName()
                )
            );
        }
    }

    @Override
    public HistoryDTO getRoomBooking(UUID historyUuid) {
        History history = getOrThrowHistory(historyUuid);
        return HistoryMapper.historyToHistoryDTO(history);
    }

    @Override
    public void confirmBooking(UUID id, UUID userUuid) {
        History history = getOrThrowHistory(id);
        List<HistoryUser> historyUserList =  history.getHistoryUsers()
                .stream()
                .filter(historyUser -> historyUser.getUserUuid().equals(userUuid))
                .collect(Collectors.toList());
        if (historyUserList.isEmpty()) {
            throw new IllegalArgumentException("No such user in history");
        }
        HistoryUser historyUser = historyUserList.get(0);
        if (historyUser.getIsAdmin()) {
            throw new IllegalArgumentException("Admin of booking can not confirm");
        }
        if (historyUser.getStatus().getName().equals(HistoryUserStatusRepository.StatusNames.CONFIRMED.name())) {
            throw new IllegalArgumentException("User already confirmed history");
        }
        HistoryUserStatus oldStatus = historyUser.getStatus();
        historyUser.setStatus(getHistoryUserStatusByName(HistoryUserStatusRepository.StatusNames.CONFIRMED.name()));
        eventBus.post(new UserStatusChangedEvent(
                HistoryMapper.historyToHistoryDTO(history),
                UserMapper.userToUserDTO(historyUser.getUser()),
                oldStatus.getName(),
                historyUser.getStatus().getName()
        ));
        historyUserRepository.save(historyUser);
    }

    private HistoryUserStatus getHistoryUserStatusByName(String name) {
        return historyUserStatusRepository.findHistoryUserStatusByName(name);
    }

    private HistoryStatus getHistoryStatusByName(String name) {
        return historyStatusRepository.findByName(name);
    }

    private Room getOrThrowRoom(UUID room_uuid) throws NoSuchElementException {
        Optional<Room> optionalRoom = roomRepository.findByUuid(room_uuid);
        if (optionalRoom.isEmpty()) {
            throw new NoSuchElementException("This room don`t exist");
        }
        return optionalRoom.get();
    }

    private User getOrThrowUser(UUID user_uuid) {
        Optional<User> optionalUser = userRepository.findById(user_uuid);
        if (optionalUser.isEmpty()) {
            throw new NoSuchElementException("This user don`t exist");
        }
        return optionalUser.get();
    }

    private History getOrThrowHistory(UUID historyUuid) throws NoSuchElementException {
        Optional<History> optionalHistory = historyRepository.findById(historyUuid);
        if (optionalHistory.isEmpty()) {
            throw new NoSuchElementException("Can`t find specified booking");
        }
        return optionalHistory.get();
    }

    private List<History> getConflictTime(List<History> historyList, OffsetDateTime start, OffsetDateTime end) {
        historyList = historyList
                .stream()
                .filter(history ->
                        (history.getEndTime().getDayOfYear() == start.getDayOfYear()) &
                                (history.getStatus().getName().equals("BOOKED"))
                )
                .collect(Collectors.toList());

        historyList = historyList.stream()
                .filter(
                        history ->
                                (
                                        (history.getStartTime().isBefore(start)) &
                                                (history.getEndTime().isAfter(start))
                                ) | (
                                        (history.getStartTime().isBefore(end)) &
                                                (history.getEndTime().isAfter(end))
                                ) | (
                                        (history.getStartTime().isAfter(start)) &
                                                (history.getStartTime().isBefore(end))
                                ) | (
                                        history.getStartTime().isEqual(start)
                                ) | (
                                        history.getEndTime().isEqual(end)
                                )
                )
                .collect(Collectors.toList());
        return historyList;
    }

    private List<Room> filterRoomsByMinMax(List<Room> rooms,
                                           Integer maxValue,
                                           Integer minValue,
                                           Function<Room, Integer> method
    ) {
        return rooms.stream()
                .filter(room -> (method.apply(room) >= minValue) & (method.apply(room) <= maxValue))
                .collect(Collectors.toList());
    }
}
