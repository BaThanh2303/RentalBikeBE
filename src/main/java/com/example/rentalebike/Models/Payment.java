package com.example.rentalebike.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "rental_id")
    private Rental rental; // liên kết với giao dịch thuê

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // người thanh toán

    private Double amount; // số tiền

    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    private Status status; // trạng thái

    public enum Status {
        PENDING,    // Đang chờ thanh toán
        SUCCESS,    // Thanh toán thành công
        FAILED,     // Thanh toán thất bại
        CANCELLED   // Hủy giao dịch
    }

    private String transactionId; // Mã giao dịch online (PayOS / Stripe)
}
