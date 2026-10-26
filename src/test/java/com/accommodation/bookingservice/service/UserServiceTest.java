package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.dto.UserProfileUpdateRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.dto.UserRoleUpdateRequestDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.UserMapper;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("updateRole: updates user role to MANAGER")
    void updateRoleExistingUserUpdatesRole() {
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        requestDto.setRole(User.Role.MANAGER);

        User user = new User();
        user.setId(1L);
        user.setRole(User.Role.CUSTOMER);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRole(User.Role.MANAGER);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setRole("MANAGER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto actual = userService.updateRole(1L, requestDto);

        assertNotNull(actual);
        assertEquals("MANAGER", actual.getRole());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateRole: non-existing user throws EntityNotFoundException")
    void updateRoleNonExistingUserThrowsException() {
        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto();
        requestDto.setRole(User.Role.MANAGER);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateRole(99L, requestDto));
    }

    @Test
    @DisplayName("getProfile: returns user profile")
    void getProfileExistingEmailReturnsProfile() {
        User user = new User();
        user.setEmail("user@example.com");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto actual = userService.getProfile("user@example.com");

        assertNotNull(actual);
        assertEquals("user@example.com", actual.getEmail());
    }

    @Test
    @DisplayName("updateProfile: updates first and last name")
    void updateProfileExistingEmailUpdatesNames() {
        UserProfileUpdateRequestDto requestDto = new UserProfileUpdateRequestDto();
        requestDto.setFirstName("Alice");
        requestDto.setLastName("Smith");

        User user = new User();
        user.setEmail("user@example.com");

        User updatedUser = new User();
        updatedUser.setEmail("user@example.com");
        updatedUser.setFirstName("Alice");
        updatedUser.setLastName("Smith");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setEmail("user@example.com");
        responseDto.setFirstName("Alice");
        responseDto.setLastName("Smith");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto actual = userService.updateProfile("user@example.com", requestDto);

        assertNotNull(actual);
        assertEquals("Alice", actual.getFirstName());
        assertEquals("Smith", actual.getLastName());
    }
}
