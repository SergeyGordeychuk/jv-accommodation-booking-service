package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.BookingRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledBookingService {
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${booking.expiration.cron:0 0 0 * * ?}")
    @Transactional
    public void checkExpiredBookings() {
        log.info("Running daily check for expired bookings...");
        LocalDate today = LocalDate.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(today);

        if (expiredBookings.isEmpty()) {
            notificationService.sendNotification("No expired bookings today!");
            return;
        }

        for (Booking booking : expiredBookings) {
            booking.setStatus(Status.EXPIRED);
            bookingRepository.save(booking);

            notificationService.sendNotification(String.format(
                    "⚠️ Booking Expired! ID: %d, Accommodation ID: %d at %s is now released.",
                    booking.getId(),
                    booking.getAccommodation().getId(),
                    booking.getAccommodation().getLocation()
            ));
        }
        log.info("Processed {} expired bookings", expiredBookings.size());
    }
}
