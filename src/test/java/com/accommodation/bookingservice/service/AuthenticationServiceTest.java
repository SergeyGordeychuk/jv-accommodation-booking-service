package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.dto.UserLoginRequestDto;
import com.accommodation.bookingservice.dto.UserLoginResponseDto;
import com.accommodation.bookingservice.dto.UserRegistrationRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.exception.RegistrationException;
import com.accommodation.bookingservice.mapper.UserMapper;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.repository.UserRepository;
import com.accommodation.bookingservice.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("register: successful registration returns UserResponseDto")
    void registerValidRequestReturnsUserResponseDto() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(requestDto.getEmail());
        savedUser.setRole(User.Role.CUSTOMER);

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setEmail("user@example.com");
        expectedResponse.setRole("CUSTOMER");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedResponse);

        UserResponseDto actual = authenticationService.register(requestDto);

        assertNotNull(actual);
        assertEquals(expectedResponse.getEmail(), actual.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: mismatched passwords throw RegistrationException")
    void registerMismatchedPasswordsThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("differentPassword");

        RegistrationException ex = assertThrows(
                RegistrationException.class,
                () -> authenticationService.register(requestDto)
        );
        assertEquals("Passwords do not match", ex.getMessage());
    }

    @Test
    @DisplayName("register: duplicate email throws RegistrationException")
    void registerDuplicateEmailThrowsException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");

        when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        RegistrationException ex = assertThrows(
                RegistrationException.class,
                () -> authenticationService.register(requestDto)
        );
        assertEquals("User with this email already exists", ex.getMessage());
    }

    @Test
    @DisplayName("login: valid credentials return jwt token")
    void loginValidCredentialsReturnsToken() {
        UserLoginRequestDto requestDto = new UserLoginRequestDto();
        requestDto.setEmail("user@example.com");
        requestDto.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt.token.value");

        UserLoginResponseDto response = authenticationService.login(requestDto);

        assertNotNull(response);
        assertEquals("jwt.token.value", response.getToken());
    }
}
