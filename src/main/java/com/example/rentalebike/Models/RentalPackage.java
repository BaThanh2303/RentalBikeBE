package com.example.rentalebike.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long packageId;

    @Column(nullable = false, unique = true)
    private String name; // Ví dụ: HOURLY, DAILY

    private String description;

    private Double price; // Giá gói (tính theo duration)

    private Integer duration; // Thời lượng (tính bằng giờ)
}
