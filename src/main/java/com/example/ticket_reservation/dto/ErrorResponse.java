package com.example.ticket_reservation.dto;

import java.time.LocalDateTime;

// Standardized JSON payload for all HTTP error responses
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}