package com.example.ticket_reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservations")
public class Reservation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String reservationCode;

    private String eventId;

    private String eventName;

    private String userId;

    private String username;

    private int seats;

    private double amount;

    private ReservationStatus status;

    private LocalDateTime createdAt;
}