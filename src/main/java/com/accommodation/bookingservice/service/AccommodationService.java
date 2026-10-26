package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.dto.AccommodationPatchRequestDto;
import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.AccommodationMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.AccommodationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;
    private final NotificationService notificationService;
    private final ImageStorageService imageStorageService;

    @Transactional
    public AccommodationResponseDto create(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toEntity(requestDto);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
        notificationService.sendNotification(String.format(
                "🏠 New Accommodation Created! ID: %d, Type: %s, Location: %s, Daily Rate: $%s",
                savedAccommodation.getId(),
                savedAccommodation.getType(),
                savedAccommodation.getLocation(),
                savedAccommodation.getDailyRate()
        ));
        return accommodationMapper.toDto(savedAccommodation);
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponseDto> findAll() {
        return accommodationRepository.findAll().stream()
                .map(accommodationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationResponseDto findById(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));
        return accommodationMapper.toDto(accommodation);
    }

    @Transactional
    public AccommodationResponseDto update(Long id, AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));
        accommodationMapper.updateEntityFromDto(requestDto, accommodation);
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updatedAccommodation);
    }

    @Transactional
    public AccommodationResponseDto partialUpdate(Long id, AccommodationPatchRequestDto patchDto) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));
        accommodationMapper.updateEntityFromPatchDto(patchDto, accommodation);
        Accommodation updatedAccommodation = accommodationRepository.save(accommodation);
        return accommodationMapper.toDto(updatedAccommodation);
    }

    @Transactional
    public AccommodationResponseDto uploadImage(Long id, MultipartFile file) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));

        String oldImageUrl = accommodation.getImageUrl();
        String newImageUrl = imageStorageService.store(file);
        accommodation.setImageUrl(newImageUrl);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
            imageStorageService.delete(oldImageUrl);
        }

        return accommodationMapper.toDto(savedAccommodation);
    }

    @Transactional
    public void delete(Long id) {
        Accommodation accommodation = accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + id
                ));
        String imageUrl = accommodation.getImageUrl();
        accommodationRepository.deleteById(id);
        if (imageUrl != null) {
            imageStorageService.delete(imageUrl);
        }
        notificationService.sendNotification(String.format(
                "🗑️ Accommodation Released/Removed! ID: %d, Location: %s",
                accommodation.getId(),
                accommodation.getLocation()
        ));
    }
}
