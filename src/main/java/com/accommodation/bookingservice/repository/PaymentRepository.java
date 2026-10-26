package com.accommodation.bookingservice.repository;

import com.accommodation.bookingservice.model.Payment;
import com.accommodation.bookingservice.model.Payment.Status;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingUserId(Long userId);

    Optional<Payment> findBySessionId(String sessionId);

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByBookingIdAndStatus(Long bookingId, Status status);

    List<Payment> findByStatus(Status status);
}
