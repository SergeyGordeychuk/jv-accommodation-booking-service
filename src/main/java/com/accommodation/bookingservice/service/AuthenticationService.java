package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.dto.UserLoginRequestDto;
import com.accommodation.bookingservice.dto.UserLoginResponseDto;
import com.accommodation.bookingservice.dto.UserRegistrationRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.exception.RegistrationException;
import com.accommodation.bookingservice.mapper.UserMapper;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.repository.UserRepository;
import com.accommodation.bookingservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public UserResponseDto register(UserRegistrationRequestDto requestDto) {
        if (!requestDto.getPassword().equals(requestDto.getRepeatPassword())) {
            throw new RegistrationException("Passwords do not match");
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole(User.Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
