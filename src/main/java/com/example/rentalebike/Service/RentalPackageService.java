package com.example.rentalebike.Service;



import com.example.rentalebike.Models.RentalPackage;
import com.example.rentalebike.Repository.RentalPackageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RentalPackageService {
    private final RentalPackageRepository rentalPackageRepository;

    public RentalPackageService(RentalPackageRepository rentalPackageRepository) {
        this.rentalPackageRepository = rentalPackageRepository;
    }

    public List<RentalPackage> getAllPackages() {
        return rentalPackageRepository.findAll();
    }

    public Optional<RentalPackage> getPackageById(Long id) {
        return rentalPackageRepository.findById(id);
    }

    public RentalPackage createPackage(RentalPackage rentalPackage) {
        return rentalPackageRepository.save(rentalPackage);
    }

    public RentalPackage updatePackage(Long id, RentalPackage updatedPackage) {
        return rentalPackageRepository.findById(id).map(pkg -> {
            pkg.setName(updatedPackage.getName());
            pkg.setDescription(updatedPackage.getDescription());
            pkg.setPrice(updatedPackage.getPrice());
            pkg.setDuration(updatedPackage.getDuration());
            return rentalPackageRepository.save(pkg);
        }).orElseThrow(() -> new RuntimeException("Package not found"));
    }

    public void deletePackage(Long id) {
        rentalPackageRepository.deleteById(id);
    }
}
