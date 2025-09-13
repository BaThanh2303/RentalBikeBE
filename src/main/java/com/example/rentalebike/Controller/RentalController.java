package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Rental> getAllRentals() {
        return rentalService.getAllRentals();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Rental> getRentalsByUser(@PathVariable Long userId) {
        return rentalService.getRentalsByUser(userId);
    }

    @PostMapping
    public Rental createRental(@RequestParam Long userId,
                               @RequestParam Long vehicleId,
                               @RequestParam Long packageId) {
        return rentalService.createRental(userId, vehicleId, packageId);
    }

    @PutMapping("/{rentalId}/return")
    public Rental returnVehicle(@PathVariable Long rentalId) {
        return rentalService.returnVehicle(rentalId);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRental(@PathVariable Long id) {
        rentalService.cancelRental(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preorder/day")
    public ResponseEntity<Map<String, Object>> preorderDay(@RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        Long vehicleId = request.get("vehicleId");
        Long packageId = request.get("packageId");

        Rental rental = rentalService.preorderDay(userId, vehicleId, packageId);

        Map<String, Object> response = Map.of(
            "rentalId", rental.getRentalId(),
            "totalCost", rental.getTotalCost(),
            "status", rental.getStatus().toString(),
            "message", "Đặt xe thành công, vui lòng thanh toán để kích hoạt"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/day")
    public ResponseEntity<Rental> confirmDay(@RequestParam Long rentalId) {
        Rental rental = rentalService.confirmDay(rentalId);
        return ResponseEntity.ok(rental);
    }
}
