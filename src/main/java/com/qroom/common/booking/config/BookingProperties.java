package com.qroom.common.booking.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Getter
public class BookingProperties {
    private final Duration maxMeetingDuration;
    private final Duration maxFutureDistance;

    public BookingProperties(
            @Value("${booking.max-meeting-duration}") Duration maxMeetingDuration,
            @Value("${booking.max-future-distance}") Duration maxFutureDistance
    ) {
        this.maxMeetingDuration = maxMeetingDuration;
        this.maxFutureDistance = maxFutureDistance;
    }
}
