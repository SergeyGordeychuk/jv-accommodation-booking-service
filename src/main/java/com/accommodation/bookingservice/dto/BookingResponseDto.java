package com.accommodation.bookingservice.dto;

import com.accommodation.bookingservice.model.Booking.Status;
import java.time.LocalDate;
import lombok.Data;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long accommodationId;
    private Long userId;
    private Status status;
}
