package com.accommodation.bookingservice.controller;

import com.accommodation.bookingservice.dto.UserLoginRequestDto;
import com.accommodation.bookingservice.dto.UserLoginResponseDto;
import com.accommodation.bookingservice.dto.UserRegistrationRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Allows users to register a new account")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto requestDto
    ) {
        UserResponseDto responseDto = authenticationService.register(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Grants JWT tokens to authenticated users")
    public ResponseEntity<UserLoginResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto requestDto
    ) {
        UserLoginResponseDto responseDto = authenticationService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
