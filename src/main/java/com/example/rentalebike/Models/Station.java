package com.example.rentalebike.Models;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    @Column(nullable = false, length = 100)
    private String name;

    private String location;

    private Double latitude;
    private Double longitude;
}
