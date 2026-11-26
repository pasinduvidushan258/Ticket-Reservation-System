package com.example.ticket_reservation.dto;

import com.example.ticket_reservation.model.Role;

// Excludes sensitive credential fields from API responses
public record UserResponse(
        String id,
        String username,
        String fullName,
        Role role
) {}