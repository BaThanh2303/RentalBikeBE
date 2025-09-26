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
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental; // liên kết với giao dịch thuê

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // người thanh toán

    @Column(nullable = false)
    private Double amount; // số tiền thanh toán

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND"; // loại tiền tệ

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String paymentMethod = "vnpay"; // phương thức thanh toán

    @Column(length = 100, unique = true)
    private String vnpayTxnRef; // Mã giao dịch VNPay

    @Column(length = 100)
    private String vnpayTransactionNo; // Mã giao dịch ngân hàng

    @Column(length = 10)
    private String vnpayResponseCode; // Mã phản hồi từ VNPay

    @Column(length = 50)
    private String vnpayBankCode; // Mã ngân hàng

    @Column(length = 20)
    private String vnpayCardType; // Loại thẻ (ATM/VISA/MASTER)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private Status status = Status.PENDING; // trạng thái thanh toán

    @Column(nullable = false)
    @Builder.Default
    private Boolean callbackProcessed = false; // đánh dấu đã xử lý callback từ VNPay

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // thời gian tạo

    @Column(nullable = false)
    private LocalDateTime updatedAt; // thời gian cập nhật

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }


    public enum Status {
        PENDING,    // Đang chờ thanh toán
        COMPLETED,  // Thanh toán thành công
        FAILED,     // Thanh toán thất bại
        CANCELLED   // Hủy giao dịch
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
