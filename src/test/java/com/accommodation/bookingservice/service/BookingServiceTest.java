package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.dto.BookingRequestDto;
import com.accommodation.bookingservice.dto.BookingResponseDto;
import com.accommodation.bookingservice.exception.BookingException;
import com.accommodation.bookingservice.mapper.BookingMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.AccommodationRepository;
import com.accommodation.bookingservice.repository.BookingRepository;
import com.accommodation.bookingservice.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("create: valid request creates booking and returns response dto")
    void createValidRequestReturnsDto() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDate.now().plusDays(1));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(3));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setAvailability(2);

        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStatus(Status.PENDING);

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(100L);
        responseDto.setStatus(Status.PENDING);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(bookingRepository.countOverlappingBookings(1L, requestDto.getCheckInDate(), requestDto.getCheckOutDate()))
                .thenReturn(0L);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(bookingMapper.toEntity(requestDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        BookingResponseDto actual = bookingService.create(requestDto, "user@test.com");

        assertNotNull(actual);
        assertEquals(100L, actual.getId());
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("create: check-out date before check-in throws BookingException")
    void createInvalidDatesThrowsBookingException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setCheckInDate(LocalDate.now().plusDays(5));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(2));

        assertThrows(BookingException.class, () ->
                bookingService.create(requestDto, "user@test.com"));
    }

    @Test
    @DisplayName("create: no available units throws BookingException")
    void createNoAvailableUnitsThrowsBookingException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setAccommodationId(1L);
        requestDto.setCheckInDate(LocalDate.now().plusDays(1));
        requestDto.setCheckOutDate(LocalDate.now().plusDays(3));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setAvailability(1);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(bookingRepository.countOverlappingBookings(1L, requestDto.getCheckInDate(), requestDto.getCheckOutDate()))
                .thenReturn(1L);

        assertThrows(BookingException.class, () ->
                bookingService.create(requestDto, "user@test.com"));
    }

    @Test
    @DisplayName("getById: owner can view own booking")
    void getByIdOwnerCanViewBooking() {
        User user = new User();
        user.setId(5L);
        user.setRole(User.Role.CUSTOMER);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(bookingMapper.toDto(booking)).thenReturn(responseDto);

        BookingResponseDto actual = bookingService.getById(1L, "user@test.com");

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    @DisplayName("getById: other customer access is denied")
    void getByIdOtherCustomerThrowsAccessDeniedException() {
        User owner = new User();
        owner.setId(5L);

        User stranger = new User();
        stranger.setId(10L);
        stranger.setRole(User.Role.CUSTOMER);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(owner);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("stranger@test.com")).thenReturn(Optional.of(stranger));

        assertThrows(AccessDeniedException.class, () ->
                bookingService.getById(1L, "stranger@test.com"));
    }

    @Test
    @DisplayName("cancel: cancels active booking and sends notification")
    void cancelActiveBookingSucceeds() {
        User user = new User();
        user.setId(5L);
        user.setRole(User.Role.CUSTOMER);

        Accommodation accommodation = new Accommodation();
        accommodation.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setStatus(Status.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        bookingService.cancel(1L, "user@test.com");

        assertEquals(Status.CANCELED, booking.getStatus());
        verify(bookingRepository).save(booking);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("cancel: already canceled booking throws BookingException")
    void cancelAlreadyCanceledThrowsException() {
        User user = new User();
        user.setId(5L);
        user.setRole(User.Role.CUSTOMER);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(Status.CANCELED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThrows(BookingException.class, () ->
                bookingService.cancel(1L, "user@test.com"));
    }
}
