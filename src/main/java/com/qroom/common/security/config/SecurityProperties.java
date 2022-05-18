package com.qroom.common.security.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Getter
public class SecurityProperties {
    private final String secret;
    private final Duration tokenLifeTime;
    private final Duration refreshTokenLifeTime;

    public SecurityProperties(
            @Value("${security.secret}") String secret,
            @Value("${security.token-life-time}") Duration tokenLifeTime,
            @Value("${security.refresh-token-life-time}") Duration refreshTokenLifeTime
    ) {
        this.secret = secret;
        this.tokenLifeTime= tokenLifeTime;
        this.refreshTokenLifeTime = refreshTokenLifeTime;
    }
}
