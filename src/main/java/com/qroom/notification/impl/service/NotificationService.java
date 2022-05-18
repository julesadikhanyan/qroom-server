package com.qroom.notification.impl.service;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.qroom.notification.api.messaging.*;
import com.qroom.room.impl.entity.HistoryUser;
import com.qroom.user.impl.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("UnstableApiUsage")
public class NotificationService implements com.qroom.notification.api.service.NotificationService {

    private final JavaMailSender emailSender;
    private final String fromEmail;
    private final Boolean isActive;

    public NotificationService(
            JavaMailSender emailSender,
            @Value("{spring.mail.username}") String fromEmail,
            @Value("${spring.mail.isActive}") Boolean isActive
    ) {
        this.emailSender = emailSender;
        this.fromEmail = fromEmail + "@gmail.com";
        this.isActive = isActive;
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        if (isActive) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Dear , ").append(event.getUser().getName()).append(".\n");
            stringBuilder.append("Admin create meeting \"").append(event.getHistory().getTitle()).append("\" ");
            stringBuilder.append("from ").append(event.getHistory().getTime().getStartTime().toString());
            stringBuilder.append(" to").append(event.getHistory().getTime().getEndTime().toString());
            sendMail(event.getUser().getLogin(), event.getHistory().getTitle(), stringBuilder.toString());
        }
    }

    @Override
    @Subscribe
    public void handleBookingChangedEvent(BookingChangedEvent event) {
        if (isActive) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Dear , ").append(event.getUser().getName()).append(".\n");
            stringBuilder.append("Admin change meeting \"").append(event.getHistory().getTitle()).append("\" ");
            stringBuilder.append("status to ").append(event.getToStatus());
            sendMail(event.getUser().getLogin(), event.getHistory().getTitle(), stringBuilder.toString());
        }
    }

    @Override
    @Subscribe
    public void handleUserStatusChangedEvent(UserStatusChangedEvent event) {

    }

    @Override
    @Subscribe
    public void handleUserCreatedEvent(UserCreatedEvent event) {

    }

    private void sendMail(String target, String subject, String message) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmail);
            simpleMailMessage.setTo(target);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            emailSender.send(simpleMailMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
