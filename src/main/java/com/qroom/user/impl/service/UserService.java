package com.qroom.user.impl.service;

import com.google.common.eventbus.EventBus;
import com.qroom.common.security.UserAuth;
import com.qroom.common.security.managers.JwtTokenManager;

import com.qroom.notification.impl.service.NotificationService;
import com.qroom.user.api.exception.IncorrectPasswordException;
import com.qroom.user.api.exception.UserAlreadyExistException;
import com.qroom.user.api.exception.UserNotFoundException;
import com.qroom.room.impl.entity.HistoryUser;
import com.qroom.user.impl.entity.User;
import com.qroom.room.impl.mapper.HistoryMapper;
import com.qroom.user.impl.mapper.UserMapper;
import com.qroom.user.impl.repository.UserRepository;
import com.qroom.user.api.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("UnstableApiUsage")
public class UserService implements com.qroom.user.api.service.UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtTokenManager tokenManager;
    private final EventBus eventBus = new EventBus();

    public UserService(UserRepository userRepository, JwtTokenManager tokenManager, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.eventBus.register(notificationService);
        this.eventBus.register(this);
    }

    @Override
    public List<HistoryDTO> getUserHistory(UUID userUuid) throws UserNotFoundException {
        User user = getOrThrowUser(userUuid);
        List<HistoryUser> historiesUserList = user.getUserHistories();
        return historiesUserList
                .stream()
                .map(HistoryUser::getHistory)
                .map(HistoryMapper::historyToHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AuthUserDTO registerUser(RegistrationDTO registrationDTO) throws UserAlreadyExistException {
        try {
            User possibleUser = getOrThrowUserByLogin(registrationDTO.getLogin());
        } catch (UserNotFoundException exception) {
            User user = UserMapper.registrationDTOToUser(registrationDTO, passwordEncoder);
            userRepository.save(user);
            return UserMapper.userToAuthUserDTO(
                    user,
                    tokenManager.generateAccessToken(user.getUserDetails()),
                    tokenManager.generateRefreshToken(user.getUserDetails())
            );
        }
        throw new UserAlreadyExistException("User with this login already exist ");
    }

    @Override
    public AuthUserDTO loginUser(LoginDTO loginDTO) throws UserNotFoundException, IncorrectPasswordException {
        User user = getOrThrowUserByLogin(loginDTO.getLogin());
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password");
        }

        return UserMapper.userToAuthUserDTO(
                user,
                tokenManager.generateAccessToken(user.getUserDetails()),
                tokenManager.generateRefreshToken(user.getUserDetails())
        );
    }

    @Override
    public TokenDTO refreshToken(Authentication authentication) throws UserNotFoundException {
        UserAuth principal = (UserAuth) authentication.getPrincipal();
        String accessToken = tokenManager.generateAccessToken(principal);
        String refreshToken = tokenManager.generateRefreshToken(principal);
        return new TokenDTO(
                accessToken,
                refreshToken
        );
    }

    @Override
    public List<UserDTO> getUsers(UUID userUuid) {
        List<User> usersList = userRepository.findAll();
        return usersList.stream()
                .map(UserMapper::userToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUser(UUID userUuid) throws UserNotFoundException {
        User user = getOrThrowUser(userUuid);
        return UserMapper.userToUserDTO(user);
    }

    private User getOrThrowUser(UUID userUuid) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findById(userUuid);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("This user don`t exist");
        }
        return optionalUser.get();
    }

    private User getOrThrowUserByLogin(String login) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findByLogin(login);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with this email don`t exist");
        }
        return optionalUser.get();
    }
}
