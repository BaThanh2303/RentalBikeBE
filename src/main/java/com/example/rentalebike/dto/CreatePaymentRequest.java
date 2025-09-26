package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long rentalId;
    private Double amount;
    private String currency = "VND";
    private String paymentMethod = "vnpay";
    private String status = "PENDING";
}