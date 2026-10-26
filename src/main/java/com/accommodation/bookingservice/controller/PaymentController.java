package com.accommodation.bookingservice.controller;

import com.accommodation.bookingservice.dto.PaymentRequestDto;
import com.accommodation.bookingservice.dto.PaymentResponseDto;
import com.accommodation.bookingservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Endpoints for handling payments via Stripe")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create payment session",
               description = "Initiates a Stripe Checkout session for a booking")
    public ResponseEntity<PaymentResponseDto> createPaymentSession(
            Authentication authentication,
            @Valid @RequestBody PaymentRequestDto requestDto
    ) {
        PaymentResponseDto responseDto = paymentService.createPaymentSession(
                requestDto, authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    @Operation(summary = "Get payments",
               description = "Retrieves payment history (own for customers, all or by user_id for managers)")
    public ResponseEntity<List<PaymentResponseDto>> getPayments(
            Authentication authentication,
            @RequestParam(value = "user_id", required = false) Long userId
    ) {
        List<PaymentResponseDto> payments = paymentService.getPayments(userId, authentication.getName());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/success")
    @Operation(summary = "Payment success callback",
               description = "Handles successful payment processing after Stripe redirection")
    public ResponseEntity<PaymentResponseDto> handleSuccess(
            @RequestParam("session_id") String sessionId
    ) {
        PaymentResponseDto responseDto = paymentService.handleSuccessfulPayment(sessionId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/cancel")
    @Operation(summary = "Payment cancel callback",
               description = "Handles payment cancellation after Stripe redirection")
    public ResponseEntity<String> handleCancel(
            @RequestParam("session_id") String sessionId
    ) {
        String message = paymentService.handleCanceledPayment(sessionId);
        return ResponseEntity.ok(message);
    }
}
