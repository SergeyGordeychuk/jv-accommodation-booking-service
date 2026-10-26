package com.accommodation.bookingservice.mapper;

import com.accommodation.bookingservice.dto.PaymentResponseDto;
import com.accommodation.bookingservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(source = "booking.id", target = "bookingId")
    PaymentResponseDto toDto(Payment payment);
}
