package com.qroom.user.api.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class RegistrationDTO {
    @NotBlank(message = "It is not allowed to create user with blank name")
    private String name;
    @Email(message = "User login should be an email")
    private String login;
    @NotBlank(message = "It is not allowed to create user with blank password")
    private String password;
}
