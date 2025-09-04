package com.example.rentalebike.Repository;

import com.example.rentalebike.Models.RentalPackage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalPackageRepository  extends JpaRepository<RentalPackage, Long> {
}
