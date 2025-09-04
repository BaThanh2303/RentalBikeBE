package com.example.rentalebike.Service;

import com.example.rentalebike.Models.Station;
import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Repository.StationRepository;
import com.example.rentalebike.Repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;

    public StationService(StationRepository stationRepository, VehicleRepository vehicleRepository) {
        this.stationRepository = stationRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public Station updateStation(Long id, Station updatedStation) {
        return stationRepository.findById(id).map(station -> {
            station.setName(updatedStation.getName());
            station.setLocation(updatedStation.getLocation());
            station.setLatitude(updatedStation.getLatitude());
            station.setLongitude(updatedStation.getLongitude());
            return stationRepository.save(station);
        }).orElseThrow(() -> new RuntimeException("Station not found"));
    }

    public void deleteStation(Long id) {
        stationRepository.deleteById(id);
    }

    //
    public Map<String, Object> getVehiclesInStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        List<Vehicle> vehicles = vehicleRepository.findByStation_StationId(stationId);

        Map<String, Object> response = new HashMap<>();
        response.put("stationId", station.getStationId());
        response.put("stationName", station.getName());
        response.put("location", station.getLocation());
        response.put("vehicles", vehicles);
        response.put("totalVehicles", vehicles.size());
        return response;
    }

    public List<Map<String, Object>> getVehicleCountForAllStations() {
        List<Station> stations = stationRepository.findAll();
        return stations.stream().map(station -> {
            List<Vehicle> vehicles = vehicleRepository.findByStation_StationId(station.getStationId());
            Map<String, Object> map = new HashMap<>();
            map.put("stationId", station.getStationId());
            map.put("stationName", station.getName());
            map.put("totalVehicles", vehicles.size());
            return map;
        }).toList();
    }
}
