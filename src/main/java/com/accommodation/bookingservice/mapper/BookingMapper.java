package com.accommodation.bookingservice.mapper;

import com.accommodation.bookingservice.dto.BookingRequestDto;
import com.accommodation.bookingservice.dto.BookingResponseDto;
import com.accommodation.bookingservice.dto.BookingUpdateDto;
import com.accommodation.bookingservice.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    Booking toEntity(BookingRequestDto requestDto);

    @Mapping(source = "accommodation.id", target = "accommodationId")
    @Mapping(source = "user.id", target = "userId")
    BookingResponseDto toDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accommodation", ignore = true)
    void updateEntityFromDto(BookingUpdateDto requestDto, @MappingTarget Booking booking);
}
