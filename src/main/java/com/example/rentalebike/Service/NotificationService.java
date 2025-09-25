package com.example.rentalebike.Service;

import com.example.rentalebike.Models.Rental;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendPickupConfirmation(Rental rental) {
        // TODO: Implement pickup notification logic
        // For now, just log the action
        System.out.println("Pickup confirmation sent for rental ID: " + rental.getRentalId());
    }

    public void sendReturnConfirmation(Rental rental) {
        // TODO: Implement return notification logic
        // For now, just log the action
        System.out.println("Return confirmation sent for rental ID: " + rental.getRentalId());
    }
}