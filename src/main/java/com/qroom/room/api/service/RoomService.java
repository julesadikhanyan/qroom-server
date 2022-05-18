package com.qroom.room.api.service;

import com.qroom.room.api.model.BookingDTO;
import com.qroom.user.api.model.HistoryDTO;
import com.qroom.room.api.model.RoomDTO;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<RoomDTO> getAllRooms();
    RoomDTO getRoom(UUID id);
    List<HistoryDTO> getRoomBookings(UUID id, Date date);
    void bookRoom(BookingDTO bookingDTO, UUID adminUuid);
    void deleteRoomBooking(UUID id, UUID userUuid);
    HistoryDTO getRoomBooking(UUID historyUuid);
    void confirmBooking(UUID id, UUID userUuid);
}
