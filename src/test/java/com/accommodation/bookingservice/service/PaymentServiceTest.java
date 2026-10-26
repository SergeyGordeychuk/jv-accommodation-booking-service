package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.dto.PaymentRequestDto;
import com.accommodation.bookingservice.dto.PaymentResponseDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.exception.PaymentException;
import com.accommodation.bookingservice.mapper.PaymentMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Payment;
import com.accommodation.bookingservice.model.Payment.Status;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.BookingRepository;
import com.accommodation.bookingservice.repository.PaymentRepository;
import com.accommodation.bookingservice.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeSecretKey", "sk_test_dummy");
        ReflectionTestUtils.setField(paymentService, "stripeSuccessUrl", "http://localhost:8080/api/payments/success");
        ReflectionTestUtils.setField(paymentService, "stripeCancelUrl", "http://localhost:8080/api/payments/cancel");
    }

    @Test
    @DisplayName("createPaymentSession: creates session and returns pending payment dto")
    void createPaymentSessionReturnsPendingPaymentDto() {
        User user = new User();
        user.setId(1L);
        user.setRole(User.Role.CUSTOMER);

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.APARTMENT);
        accommodation.setLocation("Kyiv");
        accommodation.setDailyRate(BigDecimal.valueOf(100));

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setStatus(Booking.Status.PENDING);
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(2));

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(5L);

        Payment payment = new Payment();
        payment.setId(10L);
        payment.setStatus(Status.PENDING);
        payment.setAmountToPay(BigDecimal.valueOf(200));

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);
        responseDto.setStatus(Status.PENDING);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByBookingIdAndStatus(5L, Status.PAID)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto actual = paymentService.createPaymentSession(requestDto, "user@test.com");

        assertNotNull(actual);
        assertEquals(10L, actual.getId());
        assertEquals(Status.PENDING, actual.getStatus());
    }

    @Test
    @DisplayName("createPaymentSession: already paid booking throws PaymentException")
    void createPaymentSessionAlreadyPaidThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setRole(User.Role.CUSTOMER);

        Booking booking = new Booking();
        booking.setId(5L);
        booking.setUser(user);
        booking.setStatus(Booking.Status.CONFIRMED);

        Payment existingPaidPayment = new Payment();
        existingPaidPayment.setStatus(Status.PAID);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(5L);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByBookingIdAndStatus(5L, Status.PAID)).thenReturn(Optional.of(existingPaidPayment));

        assertThrows(PaymentException.class, () ->
                paymentService.createPaymentSession(requestDto, "user@test.com"));
    }

    @Test
    @DisplayName("getPayments: customer sees only own payments")
    void getPaymentsCustomerSeesOnlyOwnPayments() {
        User user = new User();
        user.setId(3L);
        user.setRole(User.Role.CUSTOMER);

        Payment payment = new Payment();
        PaymentResponseDto responseDto = new PaymentResponseDto();

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(paymentRepository.findByBookingUserId(3L)).thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> actual = paymentService.getPayments(null, "user@test.com");

        assertEquals(1, actual.size());
    }

    @Test
    @DisplayName("handleSuccessfulPayment: updates payment to PAID, confirms booking and notifies")
    void handleSuccessfulPaymentConfirmsBookingAndNotifies() {
        Booking booking = new Booking();
        booking.setId(5L);
        booking.setStatus(Booking.Status.PENDING);

        Payment payment = new Payment();
        payment.setId(10L);
        payment.setStatus(Status.PENDING);
        payment.setSessionId("cs_test_123");
        payment.setAmountToPay(BigDecimal.valueOf(150));
        payment.setBooking(booking);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(10L);
        responseDto.setStatus(Status.PAID);

        when(paymentRepository.findBySessionId("cs_test_123")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responseDto);

        PaymentResponseDto actual = paymentService.handleSuccessfulPayment("cs_test_123");

        assertNotNull(actual);
        assertEquals(Status.PAID, payment.getStatus());
        assertEquals(Booking.Status.CONFIRMED, booking.getStatus());
        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @DisplayName("handleCanceledPayment: valid session returns cancellation message")
    void handleCanceledPaymentReturnsMessage() {
        Payment payment = new Payment();
        payment.setSessionId("cs_test_456");

        when(paymentRepository.findBySessionId("cs_test_456")).thenReturn(Optional.of(payment));

        String actual = paymentService.handleCanceledPayment("cs_test_456");

        assertNotNull(actual);
    }

    @Test
    @DisplayName("handleCanceledPayment: unknown session throws EntityNotFoundException")
    void handleCanceledPaymentMissingSessionThrowsException() {
        when(paymentRepository.findBySessionId("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                paymentService.handleCanceledPayment("unknown"));
    }
}
