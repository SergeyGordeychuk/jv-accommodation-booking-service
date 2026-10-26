package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.dto.AccommodationPatchRequestDto;
import com.accommodation.bookingservice.dto.AccommodationRequestDto;
import com.accommodation.bookingservice.dto.AccommodationResponseDto;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.AccommodationMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.repository.AccommodationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccommodationService {
    private final AccommodationRepository accommodationRepository;
    private final AccommodationMapper accommodationMapper;

    @Transactional
    public AccommodationResponseDto create(AccommodationRequestDto requestDto) {
        Accommodation accommodation = accommodationMapper.toEntity(requestDto);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);
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
    public void delete(Long id) {
        if (!accommodationRepository.existsById(id)) {
            throw new EntityNotFoundException("Accommodation not found with id: " + id);
        }
        accommodationRepository.deleteById(id);
    }
}
