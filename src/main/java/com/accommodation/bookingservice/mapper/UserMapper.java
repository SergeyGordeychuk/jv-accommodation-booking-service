package com.accommodation.bookingservice.mapper;

import com.accommodation.bookingservice.dto.UserResponseDto;
import com.accommodation.bookingservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserResponseDto toDto(User user);
}
