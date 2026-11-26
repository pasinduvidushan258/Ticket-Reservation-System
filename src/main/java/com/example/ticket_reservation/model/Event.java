package com.example.ticket_reservation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class Event {

    @Id
    private String id;

    private String name;

    private String description;

    private String venue;

    private LocalDateTime eventDate;

    private int totalSeats;

    private int availableSeats;

    private double ticketPrice;

    private String organizerId;

    private String organizerUsername;
}