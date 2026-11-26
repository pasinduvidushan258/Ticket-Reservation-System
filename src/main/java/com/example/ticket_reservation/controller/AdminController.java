package com.example.ticket_reservation.controller;

import com.example.ticket_reservation.dto.StatsResponse;
import com.example.ticket_reservation.dto.UserResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// Exposes admin-only user management and system analytics endpoints
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String roleValue = request.get("role");
        if (roleValue == null || roleValue.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Role is required");
        }

        Role newRole;
        try {
            newRole = Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleValue);
        }

        return ResponseEntity.ok(adminService.updateUserRole(id, newRole));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}