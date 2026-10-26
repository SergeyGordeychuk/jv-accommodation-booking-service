package com.accommodation.bookingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDto {
    @NotNull(message = "Booking id is required")
    private Long bookingId;
}
