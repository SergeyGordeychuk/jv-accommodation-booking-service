package com.accommodation.bookingservice.repository;

import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(Status status);

    List<Booking> findByUserIdAndStatus(Long userId, Status status);

    List<Booking> findByCheckOutDateBeforeAndStatusNot(LocalDate date, Status status);
}
