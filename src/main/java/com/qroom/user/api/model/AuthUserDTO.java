package com.qroom.user.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthUserDTO extends UserDTO {

    @JsonProperty("tokens")
    private TokenDTO tokens;

    public AuthUserDTO(TokenDTO token, UUID uuid, String login, String name) {
        super(uuid, login, name);
        this.tokens = token;
    }
}



