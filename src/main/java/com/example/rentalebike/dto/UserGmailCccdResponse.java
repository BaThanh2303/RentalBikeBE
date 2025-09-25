package com.example.rentalebike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGmailCccdResponse {
    private Long userId;
    private String name;
    private String email; // Gmail address
    private String cccdImageUrl; // CCCD image URL
}