package com.example.rentalebike;

public class RentalPricingCalculator {

    /**
     * Tính chi phí thuê xe
     * @param minutes tổng thời gian thuê (phút)
     * @param basePrice giá 30 phút đầu
     * @return tổng chi phí
     */
    public static double calculateCost(long minutes, double basePrice) {
        if (minutes <= 30) {
            return basePrice;
        } else {
            // Làm tròn lên thành bội số của 5 phút
            long roundedMinutes = ((minutes + 4) / 5) * 5;

            long extraMinutes = roundedMinutes - 30;
            long extraBlocks = extraMinutes / 5;

            return basePrice + (extraBlocks * (basePrice / 6.0));
        }
    }
}
