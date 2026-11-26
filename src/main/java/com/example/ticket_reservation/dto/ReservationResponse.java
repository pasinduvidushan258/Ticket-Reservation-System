package com.example.ticket_reservation.dto;

import com.example.ticket_reservation.model.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse(
        String id,
        String reservationCode,
        String eventId,
        String eventName,
        String userId,
        String username,
        int seats,
        double amount,
        ReservationStatus status,
        LocalDateTime createdAt
) {}