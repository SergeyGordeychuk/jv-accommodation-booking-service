package com.accommodation.bookingservice.dto;

import com.accommodation.bookingservice.model.Accommodation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccommodationPatchRequestDto {
    private Accommodation.Type type;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @Size(max = 100, message = "Size must not exceed 100 characters")
    private String size;

    @Size(max = 1000, message = "Amenities must not exceed 1000 characters")
    private String amenities;

    @DecimalMin(value = "0.01", message = "Daily rate must be greater than 0")
    private BigDecimal dailyRate;

    @Min(value = 0, message = "Availability cannot be negative")
    private Integer availability;
}
