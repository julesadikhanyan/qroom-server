package com.qroom.user.impl.mapper;

import com.qroom.user.api.model.AuthUserDTO;
import com.qroom.user.api.model.RegistrationDTO;
import com.qroom.user.api.model.TokenDTO;
import com.qroom.user.api.model.UserDTO;
import com.qroom.user.impl.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMapper {
    public static User registrationDTOToUser(RegistrationDTO registrationDTO, PasswordEncoder passwordCoder) {
        User user = new User();
        user.setLogin(registrationDTO.getLogin());
        user.setName(registrationDTO.getName());
        user.setPassword(passwordCoder.encode(registrationDTO.getPassword()));
        return user;
    }

    public static UserDTO userToUserDTO(User user){
        return new UserDTO(
                user.getUuid(),
                user.getLogin(),
                user.getName()
        );
    }

    public static AuthUserDTO userToAuthUserDTO(User user, String authToken, String refreshToken) {
        return new AuthUserDTO(
                new TokenDTO(
                        authToken,
                        refreshToken
                ),
                user.getUuid(),
                user.getLogin(),
                user.getName()
        );
    }
}
