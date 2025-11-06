package com.soen343.tbd.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.application.observer.StationSubject;
import com.soen343.tbd.domain.model.*;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.enums.EntityStatus;
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
    private final StationSubject stationPublisher;
    private final StationService stationService;
    private final EventService eventService;

    public TripService(BillRepository billRepository, TripRepository tripRepository, BikeRepository bikeRepository,
            DockRepository dockRepository, StationRepository stationRepository, StationSubject stationPublisher,
            StationService stationService, EventService eventService) {
        this.billRepository = billRepository;
        this.tripRepository = tripRepository;
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
        this.stationPublisher = stationPublisher;
        this.stationService = stationService;
        this.eventService = eventService;
    }

    // Check if a user currently has a bike rented
    @Transactional
    public Trip checkBikeRentalService(UserId userId) {
        logger.info("Starting bike rental checking process...");
        logger.info("UserId: {}", userId.value());

        // Fetch db entities
        try {
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
    // Renting a bike modifies system state (map) so need to update map and notify
    // users
    @Transactional
    public Trip rentBikeService(BikeId bikeId, DockId dockId, UserId userId, StationId stationId) {
        logger.info("Starting bike rental process...");
        logger.info("BikeId: {}, DockId: {}, UserId: {}, StationId: {}",
                bikeId.value(), dockId.value(), userId.value(), stationId.value());

        // Prevent user from renting another bike if they already have an ongoing trip
        try {
            if (tripRepository.checkRentalsByUserId(userId).isPresent()) {
                throw new RuntimeException("User already has an ongoing trip");
            }
        } catch (Exception e) {
            logger.warn("Unable to verify existing rentals for user: {}", userId.value());
            throw e;
        }

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
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            selectedBike.setStatus(BikeStatus.ON_TRIP);
            selectedBike.setDockId(null);
            bikeRepository.save(selectedBike);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            // Create event for bike rental
            eventService.createEventForEntity(EntityType.BIKE, bikeId.value(), "Bike rented by UserId: " + userId.value(),
                    previousStatus, newStatus, "User_" + String.valueOf(userId.value()));

            logger.info("Updated bike status to ON_TRIP");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved", e);
            throw new RuntimeException("Failed to update bike during rent", e);
        }

        // Update dock object
        try {
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            selectedDock.setStatus(DockStatus.EMPTY);
            dockRepository.save(selectedDock);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            // Create event for dock being freed
            eventService.createEventForEntity(EntityType.DOCK, dockId.value(), "Dock freed by UserId: " + userId.value(),
                    previousStatus, newStatus, "User_" + String.valueOf(userId.value()));

            logger.info("Updated dock status to EMPTY");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved", e);
            throw new RuntimeException("Failed to update dock during rent", e);
        }

        // Update station object
        try {
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.decrementBikesDocked();
            stationRepository.save(selectedStation);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            if (previousStatus != newStatus) {
                // Create event for station status change
                eventService.createEventForEntity(EntityType.STATION, stationId.value(),
                        "Station status changed due to bike rent by UserId: " + userId.value(),
                        previousStatus, newStatus, "User_" + String.valueOf(userId.value()));
            }
            
            // Notify all observers/users
            notifyAllUsers(selectedStation.getStationId());

            logger.info("Updated station bike count from {} to {}", currentBikes, currentBikes - 1);
        } catch (Exception e) {
            logger.warn("Station unable to be updated/saved", e);
            throw new RuntimeException("Failed to update station during rent", e);
        }

        // Create and save the trip
        Trip newTrip = null;
        try {
            newTrip = new Trip(null, bikeId, userId, stationId);
            tripRepository.save(newTrip);
            logger.info("Trip saved successfully");

            // Retrieve the generated trip with its generated id
            newTrip = tripRepository.checkRentalsByUserId(userId)
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("New Trip unable to be created", e);
            throw new RuntimeException("Failed to create trip during rent", e);
        }
        // Create event for new trip
        eventService.createEventForEntity(EntityType.TRIP, newTrip.getTripId().value(), "New trip started by UserId: " + userId.value(),
                EntityStatus.NONE, EntityStatus.ONGOING, "User_" + String.valueOf(userId.value()));

        logger.info("Bike rental completed successfully!");
        return newTrip;
    }

    // Returning a bike modifies system state (map) so need to update map and notify
    // users
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
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            selectedBike.setStatus(BikeStatus.AVAILABLE);
            selectedBike.setDockId(dockId);
            bikeRepository.save(selectedBike);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            // Create event for bike return
            eventService.createEventForEntity(EntityType.BIKE, bikeId.value(),
                    "Bike returned by UserId: " + userId.value(),
                    previousStatus, newStatus, "User_" + String.valueOf(userId.value()));

            logger.info("Updated bike status to AVAILABLE");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved", e);
            throw new RuntimeException("Failed to update bike during return", e);
        }

        // Update dock object
        try {
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            selectedDock.setStatus(DockStatus.OCCUPIED);
            dockRepository.save(selectedDock);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            // Create event for dock being occupied
            eventService.createEventForEntity(EntityType.DOCK, dockId.value(),
                    "Dock occupied by UserId: " + userId.value(),
                    previousStatus, newStatus, "User_" + String.valueOf(userId.value()));

            logger.info("Updated dock status to EMPTY");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved", e);
            throw new RuntimeException("Failed to update dock during return", e);
        }

        // Update station object
        try {
            // Determine previous status before update
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.incrementBikesDocked();
            stationRepository.save(selectedStation);

            // Determine new status after update
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            // Create event for station bike count change
            if(previousStatus != newStatus) {
                eventService.createEventForEntity(EntityType.STATION, stationId.value(),
                        "Station status changed due to bike return by UserId: " + userId.value(),
                        previousStatus, newStatus, "User_" + String.valueOf(userId.value()));
            }

            // Notify All observers
            notifyAllUsers(selectedStation.getStationId());

            logger.info("Updated station bike count from {} to {}", currentBikes, currentBikes + 1);
        } catch (Exception e) {
            logger.warn("Station unable to be updated/saved", e);
            throw new RuntimeException("Failed to update station during return", e);
        }

        // Complete the given trip and compute the bill
        Bill resultingBill = null;
        try {
            // Determine previous status before ending trip
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());

            resultingBill = currentTrip.endTrip(selectedStation.getStationId());
            tripRepository.save(currentTrip);

            // Determine new status after ending trip
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());

            // Create event for trip completion
            eventService.createEventForEntity(EntityType.TRIP, tripId.value(), "Trip ended by UserId: " + userId.value(),
                    previousStatus, newStatus, "User_" + String.valueOf(userId.value()));

            logger.info("Trip saved successfully");
        } catch (Exception e) {
            logger.warn("New Trip unable to be created", e);
            throw new RuntimeException("Failed to end trip during return", e);
        }

        // Persist the resulting bill from the trip
        try {
            billRepository.save(resultingBill);
            logger.info("Bill assigned and saved successfully");
        } catch (Exception e) {
            logger.warn("New Bill unable to be created", e);
            throw new RuntimeException("Failed to save bill during return", e);
        }

        logger.info("Bike return completed successfully!");
    }

    private void notifyAllUsers(StationId stationId) {
        try {
            stationService.getStationWithDetails(stationId.value())
                    .ifPresent(stationPublisher::notifyObservers);
            logger.debug("Notified all users about station update: {}", stationId.value());
        } catch (Exception e) {
            logger.warn("Failed to notify users for station: {}", stationId.value(), e);
        }
    }

}