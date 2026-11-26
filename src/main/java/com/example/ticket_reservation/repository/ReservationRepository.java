package com.example.ticket_reservation.repository;

import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReservationRepository extends MongoRepository<Reservation, String> {

    List<Reservation> findByUserId(String userId);

    List<Reservation> findByEventId(String eventId);

    List<Reservation> findByEventIdIn(List<String> eventIds);

    boolean existsByEventIdAndStatus(String eventId, ReservationStatus status);
}