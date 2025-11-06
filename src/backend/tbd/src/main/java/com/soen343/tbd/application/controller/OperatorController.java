package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.dto.StatusRequest;
import com.soen343.tbd.application.service.OperatorService;
import com.soen343.tbd.domain.model.enums.StationStatus;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.application.dto.OperatorRebalanceDTO;
import com.soen343.tbd.application.dto.SetMaintenanceDTO;
import com.soen343.tbd.application.dto.FetchBikeMaintenanceResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.soen343.tbd.application.dto.RemoveMaintenanceDTO;


/* 1 operator able to change station status: active/outOFservice
 2 operator can rebalance a bike (move from one dock to another) */

@RestController
@RequestMapping("/api/operator")
public class OperatorController {

    private static final Logger logger = LoggerFactory.getLogger(OperatorController.class);

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @PostMapping("/stations/status")
    public ResponseEntity<?> updateStationStatus(@RequestBody StatusRequest request) {
        // convert from dto to objects
        StationId stationId = new StationId(request.getStationId());
        StationStatus newStatus = StationStatus.valueOf(request.getStatus());

        logger.info("Starting station {} update to status {}", stationId, newStatus);

        try {
            operatorService.updateStationStatus(stationId, newStatus);
            return ResponseEntity.ok("Station: " + stationId + " status updated: " + newStatus);
        } catch (IllegalArgumentException e) {
            logger.error("Failed updating station status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Station status update broke ", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @PostMapping("/rebalance")
    public ResponseEntity<?> rebalanceBike(@RequestBody OperatorRebalanceDTO rebalancer) {
        logger.info("Starting rebalancing bike request");

        try {
            // Call the rebalanceBike service function
            operatorService.rebalanceBike(rebalancer);
            return ResponseEntity.ok("Bike has been rebalanced");
        } catch (RuntimeException e) {
            logger.error("Failed bike rebalancing: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during bike rebalancing", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @PostMapping("/maintenance/set")
    public ResponseEntity<?> setBikeForMaintenance(@RequestBody SetMaintenanceDTO setMaintenanceDTO){
        BikeId bikeId = new BikeId(setMaintenanceDTO.getBikeId());
        try {
            operatorService.setBikeForMaintenance(bikeId);
            logger.info("Bike with ID: {} set to maintenance", bikeId);
            return ResponseEntity.ok("Bike with ID: " + bikeId + " set to maintenance");
        } catch (IllegalArgumentException e) {
            logger.error("Failed setting bike to maintenance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during setting bike to maintenance", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @GetMapping("maintenance/bikes_under_maintenance")
    public ResponseEntity<?> getBikesUnderMaintenance() {
        try {
            var bikesUnderMaintenance = operatorService.getBikesUnderMaintenance();
            logger.info("Fetched bikes under maintenance successfully");
            return ResponseEntity.ok(bikesUnderMaintenance.stream()
                    .map(bike -> new FetchBikeMaintenanceResponse(bike))
                    .toList());
        } catch (Exception e) {
            logger.error("Unexpected error during fetching bikes under maintenance", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }

    @PostMapping("maintenance/remove")
    public ResponseEntity<?> removeBikeFromMaintenance(@RequestBody RemoveMaintenanceDTO removeMaintenanceDTO){
        BikeId bikeId = new BikeId(removeMaintenanceDTO.getBikeId());
        DockId dockId = new DockId(removeMaintenanceDTO.getDockId());
        try {
            operatorService.removeBikeFromMaintenance(bikeId, dockId);
            logger.info("Bike with ID: {} removed from maintenance", bikeId);
            return ResponseEntity.ok("Bike with ID: " + bikeId + " removed from maintenance");
        } catch (IllegalArgumentException e) {
            logger.error("Failed removing bike from maintenance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during removing bike from maintenance", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred");
        }
    }
    
    
}

/* dont mind this
 all the @s are spring annotations
 RestController = controller annotation, turns it into HttpResponse & rest makes it json
 RequestMapping = gets api/ url specific part
 GetMapping = get request
 PostMapping = post request & more specific url
 RequestBody = pass parameter to send with post request
 
 */