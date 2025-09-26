package com.example.rentalebike.Repository;

import com.example.rentalebike.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser_UserId(Long userId);
    List<Payment> findByRental_RentalId(Long rentalId);
    Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef);
    Optional<Payment> findFirstByRental_RentalId(Long rentalId);

    /**
     * Find payment by rental ID (alternative method name)
     * @param rentalId - Rental ID
     * @return Optional Payment
     */
    Optional<Payment> findByRentalRentalId(Long rentalId);

    /**
     * Find all payments by rental ID (in case multiple payments exist)
     * @param rentalId - Rental ID
     * @return List of Payment
     */
    List<Payment> findAllByRentalRentalId(Long rentalId);
}
