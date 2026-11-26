package com.example.ticket_reservation.service;

import com.example.ticket_reservation.dto.StatsResponse;
import com.example.ticket_reservation.dto.UserResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.ReservationStatus;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;
import com.example.ticket_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Handles administrative user management and system-wide analytics
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final AuthService authService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse updateUserRole(String userId, Role newRole) {
        if (newRole == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        AppUser currentUser = authService.getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot change your own role");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        user.setRole(newRole);
        return toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        AppUser currentUser = authService.getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot delete your own admin account");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));

        userRepository.delete(user);
    }

    public StatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();

        List<Reservation> allReservations = reservationRepository.findAll();
        long totalReservations = allReservations.size();

        List<Reservation> confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .toList();

        long ticketsSold = confirmedReservations.stream()
                .mapToLong(Reservation::getSeats)
                .sum();

        double totalRevenue = confirmedReservations.stream()
                .mapToDouble(Reservation::getAmount)
                .sum();

        return new StatsResponse(
                totalUsers,
                totalEvents,
                totalReservations,
                ticketsSold,
                totalRevenue
        );
    }

    private UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }
}