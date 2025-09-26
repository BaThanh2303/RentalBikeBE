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
import java.util.Optional;

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
                .currency("VND")
                .paymentMethod("vnpay")
                .status(Payment.Status.PENDING)
                .callbackProcessed(false)
                .build();

        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUser_UserId(userId);
    }

    // VNPay-specific methods
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef) {
        return paymentRepository.findByVnpayTxnRef(vnpayTxnRef);
    }

    public Optional<Payment> findByRentalId(Long rentalId) {
        return paymentRepository.findFirstByRental_RentalId(rentalId);
    }

    public Payment updateStatus(Long paymentId, Payment.Status status) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setStatus(status);
            return paymentRepository.save(payment);
        }).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment updateVnpayInfo(Long paymentId, String vnpayTxnRef, String vnpayTransactionNo,
                                   String vnpayResponseCode, String vnpayBankCode, String vnpayCardType) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setVnpayTxnRef(vnpayTxnRef);
            payment.setVnpayTransactionNo(vnpayTransactionNo);
            payment.setVnpayResponseCode(vnpayResponseCode);
            payment.setVnpayBankCode(vnpayBankCode);
            payment.setVnpayCardType(vnpayCardType);
            return paymentRepository.save(payment);
        }).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment markCallbackProcessed(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setCallbackProcessed(true);
            return paymentRepository.save(payment);
        }).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentsByRental(Long rentalId) {
        return paymentRepository.findByRental_RentalId(rentalId);
    }
}
