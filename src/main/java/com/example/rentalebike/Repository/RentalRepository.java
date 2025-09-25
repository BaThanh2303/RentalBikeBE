package com.example.rentalebike.Repository;


import com.example.rentalebike.Models.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserUserId(Long userId);
    List<Rental> findByStatus(Rental.Status status);
}
