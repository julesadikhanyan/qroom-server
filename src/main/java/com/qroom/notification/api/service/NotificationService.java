package com.qroom.notification.api.service;

import com.qroom.notification.api.messaging.BookingChangedEvent;
import com.qroom.notification.api.messaging.BookingCreatedEvent;
import com.qroom.notification.api.messaging.UserCreatedEvent;
import com.qroom.notification.api.messaging.UserStatusChangedEvent;

public interface NotificationService {

    void handleBookingCreatedEvent(BookingCreatedEvent event);
    void handleBookingChangedEvent(BookingChangedEvent event);
    void handleUserStatusChangedEvent(UserStatusChangedEvent event);
    void handleUserCreatedEvent(UserCreatedEvent event);
}
