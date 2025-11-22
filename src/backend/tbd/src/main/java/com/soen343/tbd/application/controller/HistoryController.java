package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.service.HistoryService;
import com.soen343.tbd.application.dto.TripDetailsDTO;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            List<Trip> trips = historyService.getAllTripsByEmail(userEmail);
            if (trips.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "No trips found for user: " + userEmail));
            }
            List<TripDetailsDTO> tripResponse = buildTrips(trips);
            // send the array to the frontend
            return ResponseEntity.ok(tripResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving trips: " + e.getMessage()));
        }
    }

    @PostMapping("/AllTripsOperator")
    public ResponseEntity<?> getAllTripsOperator() {
        try {

            // Get all trips
            List<Trip> trips = historyService.getAllTrips();
            if (trips.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "No trips found"));
            }
            // convert each obtained trip to a tripDetailsDTO
            List<TripDetailsDTO> tripResponse = buildTrips(trips);


            // send the array to the frontend
            return ResponseEntity.ok(tripResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error retrieving trips: " + e.getMessage()));
        }
    }

    public List<TripDetailsDTO> buildTrips(List<Trip> trips) {

        // convert each obtained trip to a tripDetailsDTO
        List<TripDetailsDTO> tripResponse = new ArrayList<>();

        for (Trip trip : trips) {
            BikeType bikeType = null;
            if (trip.getBikeId() != null) {
                Optional<Bike> bike = historyService.getBikeById(trip.getBikeId().value());
                if (bike.isPresent()) {
                    bikeType = bike.get().getBikeType();
                }
            }

            Double billCost = null;
            if (trip.getBillId() != null) {
                Optional<Bill> bill = historyService.getBillById(trip.getBillId().value());
                if (bill.isPresent()) {
                    billCost = bill.get().getDiscountedCost();
                }
            }

            String userName = null;
            if(trip.getUserId() != null) {
                Optional<User> user = historyService.findUserById(trip.getUserId().value());
                if (user.isPresent()) {
                    userName = user.get().getUsername();
                } else {
                    userName = "Unknown User";
                }
            }

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
                    trip.getBillId() != null ? trip.getBillId().value() : null,
                    bikeType,
                    billCost,
                    trip.getPricingStrategy() != null ? trip.getPricingStrategy().getBaseFee() : null,
                    trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPerMinuteRate() : null,
                    userName
            );
            tripResponse.add(response);
        }
        return tripResponse;
    }

}
