package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.RentalPackage;
import com.example.rentalebike.Service.RentalPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class RentalPackageController {
    private final RentalPackageService rentalPackageService;

    public RentalPackageController(RentalPackageService rentalPackageService) {
        this.rentalPackageService = rentalPackageService;
    }

    @GetMapping
    public List<RentalPackage> getAllPackages() {
        return rentalPackageService.getAllPackages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalPackage> getPackageById(@PathVariable Long id) {
        return rentalPackageService.getPackageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RentalPackage createPackage(@RequestBody RentalPackage rentalPackage) {
        return rentalPackageService.createPackage(rentalPackage);
    }

    @PutMapping("/{id}")
    public RentalPackage updatePackage(@PathVariable Long id, @RequestBody RentalPackage updatedPackage) {
        return rentalPackageService.updatePackage(id, updatedPackage);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        rentalPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
