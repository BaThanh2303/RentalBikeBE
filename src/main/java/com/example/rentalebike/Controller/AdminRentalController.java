package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rentals")
public class AdminRentalController {
    private final RentalService rentalService;

    public AdminRentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/paid-rentals")
    public ResponseEntity<?> getPaidRentals() {
        List<Rental> paidRentals = rentalService.findByStatus(Rental.Status.PAID);
        return ResponseEntity.ok(paidRentals);
    }

    @GetMapping("/active-rentals")
    public ResponseEntity<?> getActiveRentals() {
        List<Rental> activeRentals = rentalService.findByStatus(Rental.Status.ACTIVE);
        return ResponseEntity.ok(activeRentals);
    }

    @GetMapping("/completed-rentals")
    public ResponseEntity<?> getCompletedRentals() {
        List<Rental> completedRentals = rentalService.findByStatus(Rental.Status.COMPLETED);
        return ResponseEntity.ok(completedRentals);
    }
}