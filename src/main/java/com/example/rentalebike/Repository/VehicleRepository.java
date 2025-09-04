package com.example.rentalebike.Repository;

import com.example.rentalebike.Models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByStationStationId(Long stationId);
    Vehicle findByLicensePlate(String licensePlate);
    List<Vehicle> findByStation_StationId(Long stationId);
}
