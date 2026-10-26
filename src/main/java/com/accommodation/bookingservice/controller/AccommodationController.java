package com.accommodation.bookingservice.controller;

import com.accommodation.bookingservice.dto.AccommodationPatchRequestDto;
import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.service.AccommodationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@Tag(name = "Accommodation", description = "Endpoints for managing accommodations")
public class AccommodationController {
    private final AccommodationService accommodationService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Create accommodation",
               description = "Permits the addition of new accommodations (MANAGER only)")
    public ResponseEntity<AccommodationResponseDto> create(
            @Valid @RequestBody AccommodationRequestDto requestDto
    ) {
        AccommodationResponseDto responseDto = accommodationService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    @Operation(summary = "Get all accommodations",
               description = "Provides a list of available accommodations (public access)")
    public ResponseEntity<List<AccommodationResponseDto>> findAll() {
        List<AccommodationResponseDto> accommodations = accommodationService.findAll();
        return ResponseEntity.ok(accommodations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get accommodation by ID",
               description = "Retrieves detailed information about a specific accommodation")
    public ResponseEntity<AccommodationResponseDto> findById(@PathVariable Long id) {
        AccommodationResponseDto responseDto = accommodationService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update accommodation",
               description = "Allows updates to accommodation details (MANAGER only)")
    public ResponseEntity<AccommodationResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationRequestDto requestDto
    ) {
        AccommodationResponseDto responseDto = accommodationService.update(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Partially update accommodation",
               description = "Allows partial updates to accommodation details (MANAGER only)")
    public ResponseEntity<AccommodationResponseDto> partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationPatchRequestDto requestDto
    ) {
        AccommodationResponseDto responseDto = accommodationService.partialUpdate(id, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Delete accommodation",
               description = "Enables the removal of accommodations (MANAGER only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accommodationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
