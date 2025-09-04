package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Payment;
import com.example.rentalebike.Service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Tạo payment thủ công (chỉ dùng test hoặc admin tạo)
    @PostMapping
    public Payment createPayment(@RequestParam Long rentalId,
                                 @RequestParam Long userId,
                                 @RequestParam Double amount) {
        return paymentService.createPayment(rentalId, userId, amount);
    }

    // Lấy danh sách payment của 1 user
    @GetMapping("/user/{userId}")
    public List<Payment> getPaymentsByUser(@PathVariable Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    // Cập nhật trạng thái payment (giả lập callback từ PayOS)
    @PutMapping("/{paymentId}/status")
    public Payment updateStatus(@PathVariable Long paymentId,
                                @RequestParam Payment.Status status,
                                @RequestParam(required = false) String transactionId) {
        return paymentService.updateStatus(paymentId, status, transactionId);
    }
}

