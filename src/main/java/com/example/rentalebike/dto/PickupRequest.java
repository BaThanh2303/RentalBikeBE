package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PickupRequest {
    private String qrData; // QR code từ xe (in sẵn)
    private Double latitude;
    private Double longitude;
}