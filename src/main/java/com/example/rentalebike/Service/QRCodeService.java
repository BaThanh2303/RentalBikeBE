package com.example.rentalebike.Service;

import com.example.rentalebike.util.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    // Không cần tạo QR động, chỉ cần validate QR in sẵn
    public Map<String, Object> decryptQRData(String qrData) {
        try {
            // Nếu QR code là JSON
            if (qrData.startsWith("{")) {
                return JsonUtils.fromJsonToMap(qrData);
            }

            // Nếu QR code là string đơn giản
            if (qrData.startsWith("VEHICLE_")) {
                Long vehicleId = Long.parseLong(qrData.replace("VEHICLE_", ""));
                Map<String, Object> data = new HashMap<>();
                data.put("type", "vehicle_pickup");
                data.put("vehicleId", vehicleId);
                return data;
            }

            if (qrData.startsWith("STATION_")) {
                Long stationId = Long.parseLong(qrData.replace("STATION_", ""));
                Map<String, Object> data = new HashMap<>();
                data.put("type", "vehicle_return");
                data.put("stationId", stationId);
                return data;
            }

            throw new IllegalArgumentException("Invalid QR code format");

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR code: " + e.getMessage());
        }
    }
}