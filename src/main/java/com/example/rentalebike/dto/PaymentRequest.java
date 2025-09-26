package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long rentalId;
    private Double amount;
    private String currency = "VND";
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private String intent = "sale"; // Default to "sale" for immediate payment
}