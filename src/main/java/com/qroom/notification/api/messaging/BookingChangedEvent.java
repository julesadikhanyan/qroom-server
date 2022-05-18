package com.qroom.notification.api.messaging;

import com.qroom.room.impl.entity.History;
import com.qroom.user.api.model.HistoryDTO;
import com.qroom.user.api.model.UserDTO;
import com.qroom.user.impl.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BookingChangedEvent {
    private HistoryDTO history;
    private UserDTO user;
    private String fromStatus;
    private String toStatus;
}
