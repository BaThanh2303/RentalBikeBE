package com.example.rentalebike.Service;


import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Models.RentalPackage;
import com.example.rentalebike.Models.User;
import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Repository.RentalPackageRepository;
import com.example.rentalebike.Repository.RentalRepository;
import com.example.rentalebike.Repository.UserRepository;
import com.example.rentalebike.Repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalPackageRepository rentalPackageRepository;

    public RentalService(RentalRepository rentalRepository,
                         UserRepository userRepository,
                         VehicleRepository vehicleRepository,
                         RentalPackageRepository rentalPackageRepository) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.rentalPackageRepository = rentalPackageRepository;
    }

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Optional<Rental> getRentalById(Long id) {
        return rentalRepository.findById(id);
    }

    public List<Rental> getRentalsByUser(Long userId) {
        return rentalRepository.findByUserUserId(userId);
    }

    public Rental createRental(Long userId, Long vehicleId, Long packageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        RentalPackage rentalPackage = rentalPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Rental rental = Rental.builder()
                .user(user)
                .vehicle(vehicle)
                .rentalPackage(rentalPackage)
                .startTime(LocalDateTime.now())
                .status(Rental.Status.ACTIVE)
                .build();

        // Chuyển trạng thái xe sang RENTED
        vehicle.setStatus(Vehicle.Status.RENTED);
        vehicleRepository.save(vehicle);

        return rentalRepository.save(rental);
    }

    public Rental returnVehicle(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        rental.setEndTime(LocalDateTime.now());
        rental.setStatus(Rental.Status.COMPLETED);

        // Only calculate cost for hourly rentals (those that don't have totalCost set)
        // Daily rentals are prepaid and already have totalCost set
        if (rental.getTotalCost() == null) {
            // Tính phí thuê dựa trên 30p tối thiểu + block 5p (ceil)
            long minutes = java.time.Duration.between(rental.getStartTime(), rental.getEndTime()).toMinutes();

            // Nếu <= 30 phút → tính giá cơ bản
            if (minutes <= 30) {
                rental.setTotalCost(rental.getRentalPackage().getPrice());
            } else {
                // Làm tròn lên thành bội số của 5 phút
                long roundedMinutes = ((minutes + 4) / 5) * 5; // ví dụ: 32 -> 35, 36 -> 40

                // Tính số block 5 phút kể từ 30 phút
                long extraMinutes = roundedMinutes - 30;
                long extraBlocks = extraMinutes / 5;

                // Giá: basePrice (30 phút đầu) + số block * (basePrice / 6)
                double basePrice = rental.getRentalPackage().getPrice();
                double totalCost = basePrice + (extraBlocks * (basePrice / 6.0));

                rental.setTotalCost(totalCost);
            }
        }

        // Cập nhật xe về AVAILABLE
        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(Vehicle.Status.AVAILABLE);
        vehicleRepository.save(vehicle);

        return rentalRepository.save(rental);
    }

    public void cancelRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        rental.setStatus(Rental.Status.CANCELED);

        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(Vehicle.Status.AVAILABLE);
        vehicleRepository.save(vehicle);

        rentalRepository.save(rental);
    }

    public Rental preorderDay(Long userId, Long vehicleId, Long packageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        RentalPackage rentalPackage = rentalPackageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        // Validate vehicle is available
        if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
            throw new RuntimeException("Vehicle is not available");
        }

        Rental rental = Rental.builder()
                .user(user)
                .vehicle(vehicle)
                .rentalPackage(rentalPackage)
                .status(Rental.Status.PENDING)
                .totalCost(rentalPackage.getPrice())
                .build();

        return rentalRepository.save(rental);
    }

    public Rental confirmDay(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // Validate rental status is PENDING
        if (rental.getStatus() != Rental.Status.PENDING) {
            throw new RuntimeException("Rental is not in PENDING status");
        }

        // Set rental as ACTIVE with start and end times
        LocalDateTime startTime = LocalDateTime.now();
        rental.setStartTime(startTime);
        rental.setEndTime(startTime.plusHours(rental.getRentalPackage().getDuration()));
        rental.setStatus(Rental.Status.ACTIVE);

        // Update vehicle status to RENTED
        Vehicle vehicle = rental.getVehicle();
        vehicle.setStatus(Vehicle.Status.RENTED);
        vehicleRepository.save(vehicle);

        return rentalRepository.save(rental);
    }
}
