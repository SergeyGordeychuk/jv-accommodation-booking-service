package com.accommodation.bookingservice.service;

import com.accommodation.bookingservice.dto.BookingRequestDto;
import com.accommodation.bookingservice.dto.BookingResponseDto;
import com.accommodation.bookingservice.dto.BookingUpdateDto;
import com.accommodation.bookingservice.exception.BookingException;
import com.accommodation.bookingservice.exception.EntityNotFoundException;
import com.accommodation.bookingservice.mapper.BookingMapper;
import com.accommodation.bookingservice.model.Accommodation;
import com.accommodation.bookingservice.model.Booking;
import com.accommodation.bookingservice.model.Booking.Status;
import com.accommodation.bookingservice.model.User;
import com.accommodation.bookingservice.notification.NotificationService;
import com.accommodation.bookingservice.repository.AccommodationRepository;
import com.accommodation.bookingservice.repository.BookingRepository;
import com.accommodation.bookingservice.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    @Transactional
    public BookingResponseDto create(BookingRequestDto requestDto, String userEmail) {
        if (!requestDto.getCheckOutDate().isAfter(requestDto.getCheckInDate())) {
            throw new BookingException("Check-out date must be after check-in date");
        }

        Accommodation accommodation = accommodationRepository.findById(requestDto.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Accommodation not found with id: " + requestDto.getAccommodationId()
                ));

        long overlapping = bookingRepository.countOverlappingBookings(
                accommodation.getId(),
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate()
        );

        if (overlapping >= accommodation.getAvailability()) {
            throw new BookingException("No available accommodation units for the selected dates");
        }

        User user = getUserByEmail(userEmail);

        Booking booking = bookingMapper.toEntity(requestDto);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(Status.PENDING);

        Booking saved = bookingRepository.save(booking);

        notificationService.sendNotification(String.format(
                "📅 New Booking Created! ID: %d, User: %s, Dates: %s to %s, Status: %s",
                saved.getId(),
                user.getEmail(),
                saved.getCheckInDate(),
                saved.getCheckOutDate(),
                saved.getStatus()
        ));

        return bookingMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookings(Long userId, Status status, String userEmail) {
        User currentUser = getUserByEmail(userEmail);
        Long targetUserId = userId;

        if (currentUser.getRole() != User.Role.MANAGER) {
            targetUserId = currentUser.getId();
        }

        List<Booking> bookings;
        if (targetUserId != null && status != null) {
            bookings = bookingRepository.findByUserIdAndStatus(targetUserId, status);
        } else if (targetUserId != null) {
            bookings = bookingRepository.findByUserId(targetUserId);
        } else if (status != null) {
            bookings = bookingRepository.findByStatus(status);
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getMyBookings(String userEmail) {
        User user = getUserByEmail(userEmail);
        return bookingRepository.findByUserId(user.getId()).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getById(Long id, String userEmail) {
        Booking booking = findBookingById(id);
        User currentUser = getUserByEmail(userEmail);
        checkUserAccess(booking, currentUser);
        return bookingMapper.toDto(booking);
    }

    @Transactional
    public BookingResponseDto update(Long id, BookingUpdateDto requestDto, String userEmail) {
        Booking booking = findBookingById(id);
        User currentUser = getUserByEmail(userEmail);
        checkUserAccess(booking, currentUser);

        if (booking.getStatus() == Status.CANCELED || booking.getStatus() == Status.EXPIRED) {
            throw new BookingException("Cannot update a booking with status: " + booking.getStatus());
        }

        if (!requestDto.getCheckOutDate().isAfter(requestDto.getCheckInDate())) {
            throw new BookingException("Check-out date must be after check-in date");
        }

        long overlapping = bookingRepository.countOverlappingBookingsExcluding(
                booking.getAccommodation().getId(),
                booking.getId(),
                requestDto.getCheckInDate(),
                requestDto.getCheckOutDate()
        );

        if (overlapping >= booking.getAccommodation().getAvailability()) {
            throw new BookingException("No available accommodation units for the selected dates");
        }

        bookingMapper.updateEntityFromDto(requestDto, booking);
        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toDto(updated);
    }

    @Transactional
    public void cancel(Long id, String userEmail) {
        Booking booking = findBookingById(id);
        User currentUser = getUserByEmail(userEmail);
        checkUserAccess(booking, currentUser);

        if (booking.getStatus() == Status.CANCELED) {
            throw new BookingException("Booking is already canceled");
        }

        booking.setStatus(Status.CANCELED);
        bookingRepository.save(booking);

        notificationService.sendNotification(String.format(
                "❌ Booking Canceled! ID: %d, Accommodation ID: %d released",
                booking.getId(),
                booking.getAccommodation().getId()
        ));
    }

    private Booking findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    private void checkUserAccess(Booking booking, User user) {
        if (user.getRole() != User.Role.MANAGER
                && !booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied to booking with id: " + booking.getId());
        }
    }
}
