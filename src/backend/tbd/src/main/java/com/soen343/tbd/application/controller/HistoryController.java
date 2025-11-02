package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.service.HistoryService;
import com.soen343.tbd.application.dto.TripDetailsDTO;
import com.soen343.tbd.domain.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "http://localhost:3000")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PostMapping("/AllTrips")
    public ResponseEntity<?> getAllTrips(@RequestBody Map<String, Object> request) {
        try {

            String userEmail = request.get("userEmail").toString();
            System.out.println("Fetching all trips for email: " + userEmail);

            // Get all trips by email
            List<Trip> trips = historyService.getAllTripsByEmail(userEmail);

            if (trips.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "No trips found for this user"));
            }
            // convert each obtained trip to a tripDetailsDTO
            List<TripDetailsDTO> tripResponse = new ArrayList<>();

            for (Trip trip : trips) {
                // Build response DTO with trip details
                TripDetailsDTO response = new TripDetailsDTO(
                    trip.getTripId().value(),
                    trip.getBikeId() != null ? trip.getBikeId().value() : null,
                    trip.getUserId().value(),
                    trip.getStartStationId() != null ? trip.getStartStationId().value() : null,
                    trip.getEndStationId() != null ? trip.getEndStationId().value() : null,
                    trip.getStartTime() != null ? trip.getStartTime().toString() : null,
                    trip.getEndTime() != null ? trip.getEndTime().toString() : null,
                    trip.getStatus() != null ? trip.getStatus().toString() : null,
                    trip.getBillId() != null ? trip.getBillId().value() : null
                );
                tripResponse.add(response);
            }
            // send the array to the frontend
            return ResponseEntity.ok(tripResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving trips: " + e.getMessage()));
        }
    }
}
