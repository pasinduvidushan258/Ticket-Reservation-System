package com.example.ticket_reservation.service;

import com.example.ticket_reservation.dto.EventRequest;
import com.example.ticket_reservation.dto.EventResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.model.ReservationStatus;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Manages event CRUD operations, search queries, and organizer ownership rules
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final AuthService authService;

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> searchEventsByName(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse getEventById(String id) {
        Event event = findEventOrThrow(id);
        return toResponse(event);
    }

    public List<EventResponse> getMyEvents() {
        AppUser currentUser = authService.getCurrentUser();
        return eventRepository.findByOrganizerId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public EventResponse createEvent(EventRequest request) {
        AppUser currentUser = authService.getCurrentUser();

        Event event = Event.builder()
                .name(request.name())
                .description(request.description())
                .venue(request.venue())
                .eventDate(request.eventDate())
                .totalSeats(request.totalSeats())
                .availableSeats(request.totalSeats())
                .ticketPrice(request.ticketPrice())
                .organizerId(currentUser.getId())
                .organizerUsername(currentUser.getUsername())
                .build();

        return toResponse(eventRepository.save(event));
    }

    public EventResponse updateEvent(String id, EventRequest request) {
        Event event = findEventOrThrow(id);
        AppUser currentUser = authService.getCurrentUser();
        verifyOwnershipOrAdmin(event, currentUser);

        int reservedSeats = event.getTotalSeats() - event.getAvailableSeats();
        if (request.totalSeats() < reservedSeats) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Total seats cannot be less than already reserved seats (" + reservedSeats + ")"
            );
        }

        event.setName(request.name());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setEventDate(request.eventDate());
        event.setTotalSeats(request.totalSeats());
        event.setAvailableSeats(request.totalSeats() - reservedSeats);
        event.setTicketPrice(request.ticketPrice());

        return toResponse(eventRepository.save(event));
    }

    public void deleteEvent(String id) {
        Event event = findEventOrThrow(id);
        AppUser currentUser = authService.getCurrentUser();
        verifyOwnershipOrAdmin(event, currentUser);

        if (reservationRepository.existsByEventIdAndStatus(id, ReservationStatus.CONFIRMED)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete event with active confirmed reservations");
        }

        eventRepository.delete(event);
    }

    private Event findEventOrThrow(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found with id: " + id));
    }

    private void verifyOwnershipOrAdmin(Event event, AppUser user) {
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isOwner = event.getOrganizerId() != null && event.getOrganizerId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to modify this event");
        }
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getVenue(),
                event.getEventDate(),
                event.getTotalSeats(),
                event.getAvailableSeats(),
                event.getTicketPrice(),
                event.getOrganizerId(),
                event.getOrganizerUsername()
        );
    }
}