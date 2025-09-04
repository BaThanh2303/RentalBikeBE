package com.example.rentalebike.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String status = "PENDING"; // PENDING, RESOLVED

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;
}
