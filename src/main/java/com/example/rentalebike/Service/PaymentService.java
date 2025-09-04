package com.example.rentalebike.Service;


import com.example.rentalebike.Models.Payment;
import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Models.User;
import com.example.rentalebike.Repository.PaymentRepository;
import com.example.rentalebike.Repository.RentalRepository;
import com.example.rentalebike.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          RentalRepository rentalRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
    }

    public Payment createPayment(Long rentalId, Long userId, Double amount) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = Payment.builder()
                .rental(rental)
                .user(user)
                .amount(amount)
                .status(Payment.Status.PENDING)
                .paymentTime(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }


    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUser_UserId(userId);
    }

    public Payment updateStatus(Long paymentId, Payment.Status status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        if (transactionId != null) {
            payment.setTransactionId(transactionId);
        }
        return paymentRepository.save(payment);
    }
}
