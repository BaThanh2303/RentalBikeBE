package com.example.rentalebike.Service;

import com.example.rentalebike.Models.Station;
import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Repository.StationRepository;
import com.example.rentalebike.Repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;

    public VehicleService(VehicleRepository vehicleRepository, StationRepository stationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.stationRepository = stationRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getVehiclesByStation(Long stationId) {
        return vehicleRepository.findByStationStationId(stationId);
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Vehicle getVehicleByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate);
    }

    public Vehicle createVehicle(Vehicle vehicle, Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));
        vehicle.setStation(station);
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle, Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setCode(updatedVehicle.getCode());
            vehicle.setType(updatedVehicle.getType());
            vehicle.setBatteryLevel(updatedVehicle.getBatteryLevel());
            vehicle.setStatus(updatedVehicle.getStatus());
            vehicle.setLicensePlate(updatedVehicle.getLicensePlate());
            vehicle.setOwnerName(updatedVehicle.getOwnerName());
            vehicle.setStation(station);
            vehicle.setImageUrl(updatedVehicle.getImageUrl());
            return vehicleRepository.save(vehicle);
        }).orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
