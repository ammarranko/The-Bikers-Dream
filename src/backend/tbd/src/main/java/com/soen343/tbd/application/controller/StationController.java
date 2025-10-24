package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.application.service.StationService;
import com.soen343.tbd.application.observer.StationPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private static final Logger logger = LoggerFactory.getLogger(StationController.class);

    @Autowired
    private final StationPublisher stationPublisher;
    private StationService stationService;

    public StationController(StationService stationService, StationPublisher stationPublisher) {
        this.stationService = stationService;
        this.stationPublisher = stationPublisher;
    }

    /**
     * Get complete station details including all docks and bikes
     *
     * Example: GET /api/stations/1/details
     *
     * Returns:
     * - Station information (name, address, capacity, etc.)
     * - List of all docks at this station
     * - For each dock, the bike (if any) that's currently docked there
     */
    @GetMapping("/{stationId}/details")
    public ResponseEntity<StationDetailsDTO> getStationDetails(@PathVariable Long stationId) {
        logger.info("Received request to get station details for stationId: {}", stationId);

        return stationService.getStationWithDetails(stationId)
                .map(stationDetails -> {
                    logger.info("Successfully retrieved station details for stationId: {}", stationId);
                    return ResponseEntity.ok(stationDetails);
                })
                .orElseGet(() -> {
                    logger.warn("Station not found for stationId: {}", stationId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Get complete station details including all docks and bikes for all stations
     * in the db.
     *
     * Returns:
     * - List of stations each containing this info:
     * -> Station information (name, address, capacity, etc.)
     * -> List of all docks at this station
     * -> For each dock, the bike (if any) that's currently docked there
     */
    @GetMapping("/allStations/details")
    public ResponseEntity<List<StationDetailsDTO>> getAllStationsDetails() {
        logger.info("Received request to get station details for all stations");

        List<StationDetailsDTO> stationDetailsList = stationService.getAllStationsWithDetails();

        if (stationDetailsList.isEmpty()) {
            logger.warn("No stations found or details could not be retrieved");
            return ResponseEntity.notFound().build();
        }

        logger.info("Successfully retrieved details for {} stations", stationDetailsList.size());
        return ResponseEntity.ok(stationDetailsList);
    }

    // SSE endpoint for real-time subscription to station updates
    // Persistent connection and automatic notifs when station/dock/bike data
    // changes
    @GetMapping("/stream")
    public SseEmitter subscribeToStationUpdates() {
        return stationPublisher.subscribe();
    }
}