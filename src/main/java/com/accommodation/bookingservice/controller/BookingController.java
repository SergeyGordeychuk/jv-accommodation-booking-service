package com.accommodation.bookingservice.controller;

import com.accommodation.bookingservice.dto.BookingRequestDto;
import com.accommodation.bookingservice.dto.BookingResponseDto;
import com.accommodation.bookingservice.dto.BookingUpdateDto;
import com.accommodation.bookingservice.model.Booking.Status;
import com.accommodation.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Endpoints for managing bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create booking", description = "Creates a new accommodation booking")
    public ResponseEntity<BookingResponseDto> create(
            Authentication authentication,
            @Valid @RequestBody BookingRequestDto requestDto
    ) {
        BookingResponseDto responseDto = bookingService.create(requestDto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    @Operation(summary = "Get bookings",
               description = "Retrieves bookings based on user id and status (all for managers)")
    public ResponseEntity<List<BookingResponseDto>> getBookings(
            Authentication authentication,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "status", required = false) Status status
    ) {
        List<BookingResponseDto> bookings = bookingService.getBookings(
                userId, status, authentication.getName()
        );
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings",
               description = "Retrieves bookings of the current logged-in user")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(Authentication authentication) {
        List<BookingResponseDto> bookings = bookingService.getMyBookings(authentication.getName());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by id", description = "Retrieves information about a specific booking")
    public ResponseEntity<BookingResponseDto> getById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        BookingResponseDto responseDto = bookingService.getById(id, authentication.getName());
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update booking", description = "Updates details of an existing booking")
    public ResponseEntity<BookingResponseDto> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateDto requestDto
    ) {
        BookingResponseDto responseDto = bookingService.update(id, requestDto, authentication.getName());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel booking", description = "Cancels an existing booking")
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable Long id
    ) {
        bookingService.cancel(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
