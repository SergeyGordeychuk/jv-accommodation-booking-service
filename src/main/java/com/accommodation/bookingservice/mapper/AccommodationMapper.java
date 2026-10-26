package com.accommodation.bookingservice.mapper;

import com.accommodation.bookingservice.dto.AccommodationPatchRequestDto;
import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.model.Accommodation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Accommodation toEntity(AccommodationRequestDto requestDto);

    @Mapping(target = "type", expression = "java(accommodation.getType().name())")
    AccommodationResponseDto toDto(Accommodation accommodation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(AccommodationRequestDto requestDto,
                           @MappingTarget Accommodation accommodation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromPatchDto(AccommodationPatchRequestDto patchDto,
                                @MappingTarget Accommodation accommodation);
}
