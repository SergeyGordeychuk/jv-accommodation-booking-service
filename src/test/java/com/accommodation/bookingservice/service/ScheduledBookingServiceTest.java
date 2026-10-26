package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.BookingRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduledBookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ScheduledBookingService scheduledBookingService;

    @Test
    @DisplayName("checkExpiredBookings: marks bookings as EXPIRED and sends notifications")
    void checkExpiredBookingsProcessesExpiredList() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setLocation("Lviv");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(Status.PENDING);
        booking.setAccommodation(accommodation);

        when(bookingRepository.findExpiredBookings(any(LocalDate.class)))
                .thenReturn(List.of(booking));

        scheduledBookingService.checkExpiredBookings();

        assertEquals(Status.EXPIRED, booking.getStatus());
        verify(bookingRepository).save(booking);
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("checkExpiredBookings: empty list sends no-expired notification")
    void checkExpiredBookingsEmptyListSendsDefaultNotification() {
        when(bookingRepository.findExpiredBookings(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        scheduledBookingService.checkExpiredBookings();

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(notificationService).sendNotification("No expired bookings today!");
    }
}
