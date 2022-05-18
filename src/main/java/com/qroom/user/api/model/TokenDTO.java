package com.qroom.user.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenDTO {
    @JsonProperty("authenticateToken")
    String authToken;

    @JsonProperty("refreshToken")
    String refreshToken;
}
