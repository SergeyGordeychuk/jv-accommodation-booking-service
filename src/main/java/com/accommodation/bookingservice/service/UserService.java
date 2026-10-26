package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.dto.UserProfileUpdateRequestDto;
import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.dto.UserRoleUpdateRequestDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.UserMapper;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto updateRole(Long id, UserRoleUpdateRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setRole(requestDto.getRole());
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto updateProfile(String email, UserProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }
}
