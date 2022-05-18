package com.qroom.user.api.service;

import com.qroom.user.api.exception.IncorrectPasswordException;
import com.qroom.user.api.exception.UserAlreadyExistException;
import com.qroom.user.api.exception.UserNotFoundException;
import com.qroom.user.api.model.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<HistoryDTO> getUserHistory(UUID userUuid) throws UserNotFoundException;
    AuthUserDTO registerUser(RegistrationDTO registrationDTO) throws UserAlreadyExistException;
    AuthUserDTO loginUser(LoginDTO loginDTO) throws UserNotFoundException, IncorrectPasswordException;
    TokenDTO refreshToken(Authentication authentication) throws UserNotFoundException;
    List<UserDTO> getUsers(UUID userUuid);
    UserDTO getUser(UUID userUuid) throws UserNotFoundException;
}
