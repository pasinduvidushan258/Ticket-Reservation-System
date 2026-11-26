package com.example.ticket_reservation.dto;

public record StatsResponse(
        long totalUsers,
        long totalEvents,
        long totalReservations,
        long ticketsSold,
        double totalRevenue
) {}