package com.qroom.user.api.controller;

import com.qroom.common.security.UserAuth;
import com.qroom.user.api.exception.IncorrectPasswordException;
import com.qroom.user.api.exception.UserAlreadyExistException;
import com.qroom.user.api.exception.UserNotFoundException;
import com.qroom.user.api.service.UserService;
import com.qroom.user.api.model.*;
import com.qroom.user.impl.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/history")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<List<HistoryDTO>> getUserHistory(
            Authentication authentication
    ) throws UserNotFoundException {
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        return new ResponseEntity<>(service.getUserHistory(userAuth.getUuid()), HttpStatus.OK);
    }

    @PostMapping("/register")
    ResponseEntity<AuthUserDTO> registerUser(@RequestBody @Valid RegistrationDTO registrationDTO
    ) throws UserAlreadyExistException {
        return new ResponseEntity<>(service.registerUser(registrationDTO), HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    ResponseEntity<AuthUserDTO> loginUser(@RequestBody LoginDTO loginDTO
    ) throws UserNotFoundException, IncorrectPasswordException {
        return new ResponseEntity<>(service.loginUser(loginDTO), HttpStatus.OK);
    }

    @PutMapping("/authenticate/refresh")
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<TokenDTO> refreshToken(Authentication authentication
    ) throws UserNotFoundException {
        return new ResponseEntity<>(service.refreshToken(authentication), HttpStatus.OK);
    }

    @GetMapping()
    @Operation(security = {@SecurityRequirement(name = "bearerAuth")})
    ResponseEntity<List<UserDTO>> getUsers(Authentication authentication) {
        UserAuth userAuth = (UserAuth) authentication.getPrincipal();
        return new ResponseEntity<>(service.getUsers(userAuth.getUuid()), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") UUID userUuid) throws UserNotFoundException {
        return new ResponseEntity<>(service.getUser(userUuid), HttpStatus.OK);
    }
}
