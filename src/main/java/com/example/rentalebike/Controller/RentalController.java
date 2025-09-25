package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Models.Station;
import com.example.rentalebike.Service.RentalService;
import com.example.rentalebike.Service.VehicleService;
import com.example.rentalebike.Service.QRCodeService;
import com.example.rentalebike.Service.StationService;
import com.example.rentalebike.Service.NotificationService;
import com.example.rentalebike.dto.PickupRequest;
import com.example.rentalebike.dto.ReturnRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;
    private final VehicleService vehicleService;
    private final QRCodeService qrCodeService;
    private final StationService stationService;
    private final NotificationService notificationService;

    public RentalController(RentalService rentalService, VehicleService vehicleService, QRCodeService qrCodeService,
                           StationService stationService, NotificationService notificationService) {
        this.rentalService = rentalService;
        this.vehicleService = vehicleService;
        this.qrCodeService = qrCodeService;
        this.stationService = stationService;
        this.notificationService = notificationService;
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
    public ResponseEntity<?> confirmRentalPayment(@RequestParam Long rentalId) {
        try {
            Rental rental = rentalService.findById(rentalId);

            // CHỈ set status = PAID, KHÔNG set startTime/endTime
            rental.setStatus(Rental.Status.PAID);
            rental.setStartTime(null); // Quan trọng!
            rental.setEndTime(null);   // Quan trọng!

            rentalService.save(rental);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment confirmed. Ready for pickup.",
                "rental", rental
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{rentalId}/pickup-vehicle")
    public ResponseEntity<?> pickupVehicle(
            @PathVariable Long rentalId,
            @RequestBody PickupRequest request) {
        try {
            Rental rental = rentalService.findById(rentalId);

            if (rental.getStatus() != Rental.Status.PAID) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rental not ready for pickup"));
            }

            // Decode QR data from vehicle
            Map<String, Object> qrData = qrCodeService.decryptQRData(request.getQrData());

            if (!"vehicle_pickup".equals(qrData.get("type"))) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid QR code type for pickup"));
            }

            Long vehicleId = Long.valueOf(qrData.get("vehicleId").toString());

            // Validate vehicle matches rental
            if (!rental.getVehicle().getVehicleId().equals(vehicleId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "QR code does not match rental vehicle"));
            }

            // BẮT ĐẦU tính thời gian - set startTime và status = ACTIVE
            rental.setStatus(Rental.Status.ACTIVE);
            rental.setStartTime(LocalDateTime.now()); // Quan trọng!

            // Update vehicle status
            Vehicle vehicle = rental.getVehicle();
            vehicle.setStatus(Vehicle.Status.RENTED);
            vehicleService.save(vehicle);

            rentalService.save(rental);

            // Send pickup confirmation
            notificationService.sendPickupConfirmation(rental);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vehicle pickup confirmed. Usage time started.",
                "rental", rental
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{rentalId}/return-vehicle")
    public ResponseEntity<?> returnVehicle(
            @PathVariable Long rentalId,
            @RequestBody ReturnRequest request) {
        try {
            Rental rental = rentalService.findById(rentalId);

            if (rental.getStatus() != Rental.Status.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Rental not active"));
            }

            // Decode QR data from station
            Map<String, Object> qrData = qrCodeService.decryptQRData(request.getQrData());

            if (!"vehicle_return".equals(qrData.get("type"))) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid QR code type for return"));
            }

            Long stationId = Long.valueOf(qrData.get("stationId").toString());

            // Validate return station
            Station returnStation = stationService.findById(stationId);
            if (returnStation == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid return station"));
            }

            // KẾT THÚC tính thời gian - set endTime và status = COMPLETED
            rental.setStatus(Rental.Status.COMPLETED);
            rental.setEndTime(LocalDateTime.now()); // Quan trọng!

            // Tính thời gian sử dụng thực tế
            long actualDurationMinutes = 0;
            if (rental.getStartTime() != null) {
                actualDurationMinutes = ChronoUnit.MINUTES.between(rental.getStartTime(), rental.getEndTime());
            }

            // Update vehicle
            Vehicle vehicle = rental.getVehicle();
            vehicle.setStatus(Vehicle.Status.AVAILABLE);
            vehicle.setStation(returnStation); // Cập nhật vị trí xe
            vehicleService.save(vehicle);

            rentalService.save(rental);

            // Send return confirmation
            notificationService.sendReturnConfirmation(rental);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vehicle returned successfully.",
                "rental", rental,
                "actualDurationMinutes", actualDurationMinutes
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }

}
