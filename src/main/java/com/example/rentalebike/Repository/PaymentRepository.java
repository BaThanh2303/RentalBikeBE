package com.example.rentalebike.Repository;

import com.example.rentalebike.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser_UserId(Long userId);
    List<Payment> findByRental_RentalId(Long rentalId);
}
