package com.soen343.tbd.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.domain.model.*;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.domain.repository.*;


@Service
public class TripService {
    private static final Logger logger = LoggerFactory.getLogger(TripService.class);

    private final BillRepository billRepository;
    private final TripRepository tripRepository;
    private final BikeRepository bikeRepository;
    private final DockRepository dockRepository;
    private final StationRepository stationRepository;

    public TripService(BillRepository billRepository, TripRepository tripRepository, BikeRepository bikeRepository, 
                       DockRepository dockRepository, StationRepository stationRepository){
        this.billRepository = billRepository;
        this.tripRepository = tripRepository;
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
    }

    // Check if a user currently has a bike rented
    @Transactional
    public Trip checkBikeRentalService(UserId userId) {
        logger.info("Starting bike rental checking process...");
        logger.info("UserId: {}", userId.value());

        // Fetch db entities
        try{
            Trip existingTrip = tripRepository.checkRentalsByUserId(userId)
                .orElse(null);
            logger.info("User rentals searched successfully.");
            return existingTrip;
        } catch (Exception e) {
            logger.warn("User rentals unable to be searched");
            return null;
        }
    }

    // Allow a user to rent a bike and update all necessary values
    @Transactional
    public Trip rentBikeService(BikeId bikeId, DockId dockId, UserId userId, StationId stationId) {
        logger.info("Starting bike rental process...");
        logger.info("BikeId: {}, DockId: {}, UserId: {}, StationId: {}", 
                    bikeId.value(), dockId.value(), userId.value(), stationId.value());
        
        // Fetch entities
        Bike selectedBike = bikeRepository.findById(bikeId)
            .orElseThrow(() -> new RuntimeException("Bike Not found with ID: " + bikeId.value()));
        Dock selectedDock = dockRepository.findById(dockId)
            .orElseThrow(() -> new RuntimeException("Dock Not found with ID: " + dockId.value()));
        Station selectedStation = stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Station Not found with ID: " + stationId.value()));
        
        logger.info("Found bike: {}, dock: {}, station: {}", 
                    selectedBike.getBikeId().value(), 
                    selectedDock.getDockId().value(), 
                    selectedStation.getStationId().value());
        
        // Update bike object
        try {
            selectedBike.setStatus(BikeStatus.ON_TRIP);
            selectedBike.setDockId(null);
            bikeRepository.save(selectedBike);
            logger.info("Updated bike status to ON_TRIP");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved");
        }


        // Update dock object
        try {
            selectedDock.setStatus(DockStatus.EMPTY);
            dockRepository.save(selectedDock);
            logger.info("Updated dock status to EMPTY");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved");
        }


        // Update station object
        try {
            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.decrementBikesDocked();
            stationRepository.save(selectedStation);
            logger.info("Updated station bike count from {} to {}", currentBikes, currentBikes - 1);            
        } catch (Exception e) {
            logger.warn("Station unable to be updated/saved");
        }


        // Create and save the trip
        Trip newTrip = null;
        try {
            newTrip = new Trip(null, bikeId, userId, stationId);
            tripRepository.save(newTrip);
            logger.info("Trip saved successfully");

            // Retrieve the generated trip with its generated
            newTrip = tripRepository.checkRentalsByUserId(userId)
                .orElse(null);
        } catch(Exception e) {
            logger.warn("New Trip unable to be created");
        }
        
        logger.info("Bike rental completed successfully!");
        return newTrip;
    }

    @Transactional
    public void returnBikeService(TripId tripId, BikeId bikeId, DockId dockId, UserId userId, StationId stationId) {
        logger.info("Starting bike return process...");
        logger.info("BikeId: {}, DockId: {}, UserId: {}, StationId: {}", 
                    bikeId.value(), dockId.value(), userId.value(), stationId.value());
        
        // Fetch entities
        Trip currentTrip = tripRepository.findById(tripId)
            .orElseThrow(() -> new RuntimeException("Trip Not found with ID: " + tripId.value()));
        Bike selectedBike = bikeRepository.findById(bikeId)
            .orElseThrow(() -> new RuntimeException("Bike Not found with ID: " + bikeId.value()));
        Dock selectedDock = dockRepository.findById(dockId)
            .orElseThrow(() -> new RuntimeException("Dock Not found with ID: " + dockId.value()));
        Station selectedStation = stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Station Not found with ID: " + stationId.value()));
        
        logger.info("Found bike: {}, dock: {}, station: {}", 
                    selectedBike.getBikeId().value(), 
                    selectedDock.getDockId().value(), 
                    selectedStation.getStationId().value());
        
        // Update bike object
        try {
            selectedBike.setStatus(BikeStatus.AVAILABLE);
            selectedBike.setDockId(dockId);
            bikeRepository.save(selectedBike);
            logger.info("Updated bike status to AVAILABLE");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved");
        }


        // Update dock object
        try {
            selectedDock.setStatus(DockStatus.OCCUPIED);
            dockRepository.save(selectedDock);
            logger.info("Updated dock status to EMPTY");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved");
        }


        // Update station object
        try {
            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.incrementBikesDocked();
            stationRepository.save(selectedStation);
            logger.info("Updated station bike count from {} to {}", currentBikes, currentBikes + 1);            
        } catch (Exception e) {
            logger.warn("Station unable to be updated/saved");
        }


        // Complete the given trip and compute the bill
        Bill resultingBill = null;
        try {
            resultingBill = currentTrip.endTrip(selectedStation.getStationId());
            tripRepository.save(currentTrip);
            logger.info("Trip saved successfully");
        } catch(Exception e) {
            logger.warn("New Trip unable to be created");
        }

        // Persist the resulting bill from the trip
        try {
            billRepository.save(resultingBill);
            logger.info("Bill assigned and saved successfully");
        } catch(Exception e) {
            logger.warn("New Bill unable to be created");
        }
        
        logger.info("Bike return completed successfully!");
    }
}