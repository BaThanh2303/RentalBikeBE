package com.example.rentalebike.Models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @Column(nullable = false, unique = true)
    private String code; // Mã định danh xe (QR code)

    @Column(nullable = false)
    private String type; // "E-Scooter", "E-Bike"

    private Double batteryLevel; // % pin

    @Enumerated(EnumType.STRING)
    private Status status = Status.AVAILABLE;

    @Column(length = 20, unique = true)
    private String licensePlate; // Biển số xe

    @Column(length = 100)
    private String ownerName; // Tên chủ xe (quản lý/thuộc về ai)

    public enum Status {
        AVAILABLE, RENTED, MAINTENANCE
    }

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;
    private String imageUrl;
}
