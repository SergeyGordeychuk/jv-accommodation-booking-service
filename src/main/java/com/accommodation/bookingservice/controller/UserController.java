package com.accommodation.bookingservice.controller;

import com.accommodation.bookingservice.dto.UserProfileUpdateRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.dto.UserRoleUpdateRequestDto;
import com.accommodation.bookingservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for user management")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user role",
               description = "Enables managers to update user roles")
    public ResponseEntity<UserResponseDto> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequestDto requestDto
    ) {
        UserResponseDto responseDto = userService.updateRole(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
               description = "Retrieves the profile information for the currently logged-in user")
    public ResponseEntity<UserResponseDto> getProfile(Authentication authentication) {
        UserResponseDto responseDto = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
               description = "Allows users to update their profile information")
    public ResponseEntity<UserResponseDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        UserResponseDto responseDto = userService.updateProfile(authentication.getName(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/me")
    @Operation(summary = "Partially update current user profile",
               description = "Allows users to partially update their profile information")
    public ResponseEntity<UserResponseDto> patchProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        UserResponseDto responseDto = userService.updateProfile(authentication.getName(), requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
