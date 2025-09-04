package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Station;
import com.example.rentalebike.Service.StationService;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Station createStation(@RequestBody Station station) {
        return stationService.createStation(station);
    }

    @PutMapping("/{id}")
    public Station updateStation(@PathVariable Long id, @RequestBody Station updatedStation) {
        return stationService.updateStation(id, updatedStation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{stationId}/vehicles")
    public Map<String, Object> getVehiclesInStation(@PathVariable Long stationId) {
        return stationService.getVehiclesInStation(stationId);
    }

    @GetMapping("/with-vehicles")
    public List<Map<String, Object>> getVehicleCountForAllStations() {
        return stationService.getVehicleCountForAllStations();
    }
}
