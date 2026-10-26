package com.accommodation.bookingservice.repository;

import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(Status status);

    List<Booking> findByUserIdAndStatus(Long userId, Status status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND b.status NOT IN ('CANCELED', 'EXPIRED') "
            + "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    long countOverlappingBookings(@Param("accommodationId") Long accommodationId,
                                  @Param("checkInDate") LocalDate checkInDate,
                                  @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND b.id <> :bookingId "
            + "AND b.status NOT IN ('CANCELED', 'EXPIRED') "
            + "AND b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate")
    long countOverlappingBookingsExcluding(@Param("accommodationId") Long accommodationId,
                                           @Param("bookingId") Long bookingId,
                                           @Param("checkInDate") LocalDate checkInDate,
                                           @Param("checkOutDate") LocalDate checkOutDate);

    @Query("SELECT b FROM Booking b WHERE b.status IN ('PENDING', 'CONFIRMED') "
            + "AND b.checkOutDate <= :date")
    List<Booking> findExpiredBookings(@Param("date") LocalDate date);
}
