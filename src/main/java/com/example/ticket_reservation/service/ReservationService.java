package com.example.ticket_reservation.service;

import com.example.ticket_reservation.dto.ReservationRequest;
import com.example.ticket_reservation.dto.ReservationResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.ReservationStatus;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Handles ticket booking, seat inventory updates, cancellations, and reservation queries
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final AuthService authService;

    public ReservationResponse createReservation(ReservationRequest request) {
        AppUser currentUser = authService.getCurrentUser();
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found with id: " + request.eventId()));

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot reserve tickets for a past event");
        }

        if (event.getAvailableSeats() < request.seats()) {
            throw new ApiException(HttpStatus.CONFLICT, "Not enough available seats");
        }

        event.setAvailableSeats(event.getAvailableSeats() - request.seats());
        eventRepository.save(event);

        String code = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalAmount = event.getTicketPrice() * request.seats();

        Reservation reservation = Reservation.builder()
                .reservationCode(code)
                .eventId(event.getId())
                .eventName(event.getName())
                .userId(currentUser.getId())
                .username(currentUser.getUsername())
                .seats(request.seats())
                .amount(totalAmount)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(reservationRepository.save(reservation));
    }

    public List<ReservationResponse> getMyReservations() {
        AppUser currentUser = authService.getCurrentUser();
        return reservationRepository.findByUserId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservationResponse> getOrganizerReservations() {
        AppUser currentUser = authService.getCurrentUser();
        List<String> eventIds = eventRepository.findByOrganizerId(currentUser.getId()).stream()
                .map(Event::getId)
                .toList();

        return reservationRepository.findByEventIdIn(eventIds).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservationResponse> getReservationsForEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found with id: " + eventId));

        AppUser currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = event.getOrganizerId() != null && event.getOrganizerId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to view reservations for this event");
        }

        return reservationRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ReservationResponse cancelReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found with id: " + reservationId));

        AppUser currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = reservation.getUserId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to cancel this reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation updatedReservation = reservationRepository.save(reservation);

        eventRepository.findById(reservation.getEventId()).ifPresent(event -> {
            int restoredSeats = Math.min(event.getTotalSeats(), event.getAvailableSeats() + reservation.getSeats());
            event.setAvailableSeats(restoredSeats);
            eventRepository.save(event);
        });

        return toResponse(updatedReservation);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getEventId(),
                reservation.getEventName(),
                reservation.getUserId(),
                reservation.getUsername(),
                reservation.getSeats(),
                reservation.getAmount(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}