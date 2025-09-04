package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/station/{stationId}")
    public List<Vehicle> getVehiclesByStation(@PathVariable Long stationId) {
        return vehicleService.getVehiclesByStation(stationId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/license/{licensePlate}")
    public ResponseEntity<Vehicle> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        Vehicle vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        return vehicle != null ? ResponseEntity.ok(vehicle) : ResponseEntity.notFound().build();
    }

    @PostMapping("/station/{stationId}")
    public Vehicle createVehicle(@RequestBody Vehicle vehicle, @PathVariable Long stationId) {
        return vehicleService.createVehicle(vehicle, stationId);
    }

    @PutMapping("/{id}/station/{stationId}")
    public Vehicle updateVehicle(@PathVariable Long id, @RequestBody Vehicle updatedVehicle, @PathVariable Long stationId) {
        return vehicleService.updateVehicle(id, updatedVehicle, stationId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
