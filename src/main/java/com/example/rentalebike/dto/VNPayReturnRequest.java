package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VNPayReturnRequest {
    private String vnpResponseCode;
    private String vnpTxnRef;
    private String vnpAmount;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private String vnpCardType;
    private String vnpSecureHash;
}