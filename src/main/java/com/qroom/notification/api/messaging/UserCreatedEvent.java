package com.qroom.notification.api.messaging;

import com.qroom.user.api.model.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserCreatedEvent {
    private UserDTO user;
}
