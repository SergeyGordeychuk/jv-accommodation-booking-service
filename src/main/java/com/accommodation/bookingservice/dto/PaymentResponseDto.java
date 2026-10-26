package com.accommodation.bookingservice.dto;

import com.accommodation.bookingservice.model.Payment.Status;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private Status status;
    private Long bookingId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}
