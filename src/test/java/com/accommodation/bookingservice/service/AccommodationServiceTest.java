package com.accommodation.bookingservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accommodation.bookingservice.dto.AccommodationPatchRequestDto;
import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.AccommodationMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.AccommodationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {
    @Mock
    private AccommodationRepository accommodationRepository;
    @Mock
    private AccommodationMapper accommodationMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private AccommodationService accommodationService;

    @Test
    @DisplayName("create: saves accommodation and returns response dto")
    void createValidRequestReturnsDto() {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        requestDto.setType(Accommodation.Type.HOUSE);
        requestDto.setLocation("Kyiv");
        requestDto.setSize("2BR");
        requestDto.setDailyRate(BigDecimal.valueOf(100));
        requestDto.setAvailability(2);

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);

        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);
        responseDto.setLocation("Kyiv");

        when(accommodationMapper.toEntity(requestDto)).thenReturn(accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        AccommodationResponseDto actual = accommodationService.create(requestDto);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        verify(accommodationRepository).save(accommodation);
    }

    @Test
    @DisplayName("findAll: returns list of response dtos")
    void findAllReturnsAllAccommodations() {
        Accommodation accommodation = new Accommodation();
        AccommodationResponseDto responseDto = new AccommodationResponseDto();

        when(accommodationRepository.findAll()).thenReturn(List.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        List<AccommodationResponseDto> actual = accommodationService.findAll();

        assertEquals(1, actual.size());
    }

    @Test
    @DisplayName("findById: existing id returns response dto")
    void findByIdExistingIdReturnsDto() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        AccommodationResponseDto actual = accommodationService.findById(1L);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    @DisplayName("findById: missing id throws EntityNotFoundException")
    void findByIdMissingIdThrowsException() {
        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accommodationService.findById(99L));
    }

    @Test
    @DisplayName("update: existing id updates and returns response dto")
    void updateExistingIdUpdatesAndReturnsDto() {
        AccommodationRequestDto requestDto = new AccommodationRequestDto();
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        doNothing().when(accommodationMapper).updateEntityFromDto(requestDto, accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        AccommodationResponseDto actual = accommodationService.update(1L, requestDto);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    @DisplayName("partialUpdate: updates only non-null fields")
    void partialUpdateExistingIdUpdatesAndReturnsDto() {
        AccommodationPatchRequestDto patchDto = new AccommodationPatchRequestDto();
        patchDto.setLocation("New location");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);
        responseDto.setLocation("New location");

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        doNothing().when(accommodationMapper).updateEntityFromPatchDto(patchDto, accommodation);
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        AccommodationResponseDto actual = accommodationService.partialUpdate(1L, patchDto);

        assertNotNull(actual);
        assertEquals("New location", actual.getLocation());
    }

    @Test
    @DisplayName("delete: existing id deletes accommodation")
    void deleteExistingIdDeletesSuccessfully() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setLocation("Kyiv");
        accommodation.setImageUrl("/uploads/test.jpg");

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        doNothing().when(accommodationRepository).deleteById(1L);

        accommodationService.delete(1L);

        verify(accommodationRepository).deleteById(1L);
        verify(imageStorageService).delete("/uploads/test.jpg");
        verify(notificationService).sendNotification(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("delete: non-existing id throws EntityNotFoundException")
    void deleteNonExistingIdThrowsException() {
        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accommodationService.delete(99L));
    }

    @Test
    @DisplayName("uploadImage: stores new file and updates accommodation")
    void uploadImageStoresNewFileAndUpdatesEntity() {
        MultipartFile file = mock(MultipartFile.class);
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setImageUrl("/uploads/old.jpg");

        AccommodationResponseDto responseDto = new AccommodationResponseDto();
        responseDto.setId(1L);
        responseDto.setImageUrl("/uploads/new.jpg");

        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(imageStorageService.store(file)).thenReturn("/uploads/new.jpg");
        when(accommodationRepository.save(accommodation)).thenReturn(accommodation);
        when(accommodationMapper.toDto(accommodation)).thenReturn(responseDto);

        AccommodationResponseDto actual = accommodationService.uploadImage(1L, file);

        assertNotNull(actual);
        assertEquals("/uploads/new.jpg", actual.getImageUrl());
        verify(imageStorageService).delete("/uploads/old.jpg");
        verify(accommodationRepository).save(accommodation);
    }

    @Test
    @DisplayName("uploadImage: throws exception when accommodation not found")
    void uploadImageThrowsExceptionWhenAccommodationNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accommodationService.uploadImage(99L, file));
    }
}
