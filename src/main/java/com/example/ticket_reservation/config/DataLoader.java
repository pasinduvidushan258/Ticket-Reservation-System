package com.example.ticket_reservation.config;

import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// Seeds initial users and sample events into MongoDB when collections are empty
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .build();

            AppUser organizer = AppUser.builder()
                    .username("organizer")
                    .password(passwordEncoder.encode("organizer123"))
                    .fullName("Event Organizer")
                    .role(Role.ORGANIZER)
                    .build();

            AppUser customer = AppUser.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("John Customer")
                    .role(Role.CUSTOMER)
                    .build();

            userRepository.save(admin);
            AppUser savedOrganizer = userRepository.save(organizer);
            userRepository.save(customer);

            log.info("Seeded default users: admin, organizer, customer");

            if (eventRepository.count() == 0) {
                Event concert = Event.builder()
                        .name("Colombo Symphony Night")
                        .description("Live orchestral performance at Nelum Pokuna")
                        .venue("Nelum Pokuna Theatre, Colombo")
                        .eventDate(LocalDateTime.now().plusDays(14))
                        .totalSeats(100)
                        .availableSeats(100)
                        .ticketPrice(3500.0)
                        .organizerId(savedOrganizer.getId())
                        .organizerUsername(savedOrganizer.getUsername())
                        .build();

                Event techSummit = Event.builder()
                        .name("Sri Lanka Cloud & AI Summit")
                        .description("Annual developer conference and hands-on workshops")
                        .venue("BMICH, Colombo")
                        .eventDate(LocalDateTime.now().plusDays(30))
                        .totalSeats(250)
                        .availableSeats(250)
                        .ticketPrice(2000.0)
                        .organizerId(savedOrganizer.getId())
                        .organizerUsername(savedOrganizer.getUsername())
                        .build();

                eventRepository.saveAll(List.of(concert, techSummit));
                log.info("Seeded sample events for organizer");
            }
        }
    }
}