package com.example.ticket_reservation.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// Custom runtime exception carrying specific HTTP status codes (e.g., 400, 404, 409)
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}