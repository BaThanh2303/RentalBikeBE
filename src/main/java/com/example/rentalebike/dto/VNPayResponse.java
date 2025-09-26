package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VNPayResponse {
    private boolean success;
    private String paymentUrl;
    private String txnRef;
    private String status;
    private String message;
    private String responseCode;
    private Double amount;
    private String currency;
    private String orderInfo;
}