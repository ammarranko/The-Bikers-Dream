package com.soen343.tbd.application.service;

import java.util.HashMap;
import java.util.List;

import java.util.Map;

import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.pricing.EBikePricing;
import com.soen343.tbd.domain.model.pricing.StandardBikePricing;
import com.soen343.tbd.domain.model.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.application.observer.SSEStationObserver;
import com.soen343.tbd.application.observer.StationSubject;
import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.domain.model.*;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.domain.repository.*;
import com.soen343.tbd.application.observer.StationSubject;

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
    private final SSEStationObserver sseStationObserver;
    private final UserRepository userRepository;

    public TripService(BillRepository billRepository, TripRepository tripRepository, BikeRepository bikeRepository,
            DockRepository dockRepository, StationRepository stationRepository, StationSubject stationPublisher,
            StationService stationService, EventService eventService, SSEStationObserver sseStationObserver,
            UserRepository userRepository) {
        this.billRepository = billRepository;
        this.tripRepository = tripRepository;
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
        this.stationPublisher = stationPublisher;
        this.stationService = stationService;
        this.eventService = eventService;
        this.sseStationObserver = sseStationObserver;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to safely create and notify about events
     * Logs but doesn't throw if event creation fails
     */
    private void safelyCreateAndNotifyEvent(EntityType entityType, Long entityId, String metadata,
            EntityStatus previousState, EntityStatus newState, String triggeredBy) {
        try {
            Event event = eventService.createEventForEntity(entityType, entityId, metadata,
                    previousState, newState, triggeredBy);

            if (event != null) {
                try {
                    EventDTO eventDTO = EventDTO.fromEvent(event);
                    eventService.notifyAllOperatorsWithEvent(eventDTO);
                } catch (Exception e) {
                    logger.warn("Failed to convert or notify event for {} #{}: {}",
                            entityType, entityId, e.getMessage());
                }
            } else {
                logger.warn("Event creation returned null for {} #{}", entityType, entityId);
            }
        } catch (Exception e) {
            logger.error("Exception during event creation for {} #{}: {}",
                    entityType, entityId, e.getMessage(), e);
        }
    }

    @Transactional
    public Trip checkBikeRentalService(UserId userId) {
        logger.info("Starting bike rental checking process...");
        logger.info("UserId: {}", userId.value());

        try {
            Trip existingTrip = tripRepository.checkRentalsByUserId(userId).orElse(null);
            logger.info("User rentals searched successfully.");
            return existingTrip;
        } catch (Exception e) {
            logger.warn("User rentals unable to be searched");
            return null;
        }
    }

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
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());
            selectedBike.setStatus(BikeStatus.ON_TRIP);
            selectedBike.setDockId(null);
            bikeRepository.save(selectedBike);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            // Create event - but don't fail the rental if event creation fails
            safelyCreateAndNotifyEvent(EntityType.BIKE, bikeId.value(),
                    "Bike rented by user", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Updated bike status to ON_TRIP");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved", e);
            throw new RuntimeException("Failed to update bike during rent", e);
        }

        // Update dock object
        try {
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());
            selectedDock.setStatus(DockStatus.EMPTY);
            dockRepository.save(selectedDock);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            safelyCreateAndNotifyEvent(EntityType.DOCK, dockId.value(),
                    "Dock freed by user", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Updated dock status to EMPTY");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved", e);
            throw new RuntimeException("Failed to update dock during rent", e);
        }

        // Update station object
        try {
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());
            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.decrementBikesDocked();
            stationRepository.save(selectedStation);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            if (previousStatus != newStatus) {
                safelyCreateAndNotifyEvent(EntityType.STATION, stationId.value(),
                        "Station status changed due to bike rental",
                        previousStatus, newStatus, "User_" + userId.value());
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
            if (selectedBike.getBikeType().equals(BikeType.E_BIKE)) {
                newTrip = new Trip(null, bikeId, userId, stationId, new EBikePricing());
            } else {
                newTrip = new Trip(null, bikeId, userId, stationId, new StandardBikePricing());
            }

            newTrip = tripRepository.save(newTrip);
            logger.info("Trip saved successfully");

            newTrip = tripRepository.checkRentalsByUserId(userId).orElse(null);

            if (newTrip != null) {
                safelyCreateAndNotifyEvent(EntityType.TRIP, newTrip.getTripId().value(),
                        "New trip started", EntityStatus.NONE, EntityStatus.ONGOING,
                        "User_" + userId.value());
            }
        } catch (Exception e) {
            logger.warn("New Trip unable to be created", e);
            throw new RuntimeException("Failed to create trip during rent", e);
        }
        // Create event for new trip
        eventService.createEventForEntity(EntityType.TRIP, newTrip.getTripId().value(),
                "New trip started by UserId: " + userId.value(),
                EntityStatus.NONE, EntityStatus.ONGOING, "User_" + String.valueOf(userId.value()));

        logger.info("Bike rental completed successfully!");
        return newTrip;
    }

    @Transactional
    public Map<String, Object> returnBikeService(TripId tripId, BikeId bikeId, DockId dockId, UserId userId,
            StationId stationId) {
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
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());
            selectedBike.setStatus(BikeStatus.AVAILABLE);
            selectedBike.setDockId(dockId);
            bikeRepository.save(selectedBike);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedBike.getStatus());

            safelyCreateAndNotifyEvent(EntityType.BIKE, bikeId.value(),
                    "Bike returned by user", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Updated bike status to AVAILABLE");
        } catch (Exception e) {
            logger.warn("Bike unable to be updated/saved", e);
            throw new RuntimeException("Failed to update bike during return", e);
        }

        // Update dock object
        try {
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());
            selectedDock.setStatus(DockStatus.OCCUPIED);
            dockRepository.save(selectedDock);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedDock.getStatus());

            safelyCreateAndNotifyEvent(EntityType.DOCK, dockId.value(),
                    "Dock occupied by returned bike", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Updated dock status to OCCUPIED");
        } catch (Exception e) {
            logger.warn("Dock unable to be updated/saved", e);
            throw new RuntimeException("Failed to update dock during return", e);
        }

        // Update station object
        try {
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());
            int currentBikes = selectedStation.getNumberOfBikesDocked();
            selectedStation.incrementBikesDocked();
            stationRepository.save(selectedStation);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(selectedStation.getStationStatus());

            if (previousStatus != newStatus) {
                safelyCreateAndNotifyEvent(EntityType.STATION, stationId.value(),
                        "Station status changed due to bike return",
                        previousStatus, newStatus, "User_" + userId.value());
            }

            notifyAllUsers(selectedStation.getStationId());

            logger.info("Updated station bike count from {} to {}", currentBikes, currentBikes + 1);
        } catch (Exception e) {
            logger.warn("Station unable to be updated/saved", e);
            throw new RuntimeException("Failed to update station during return", e);
        }

        // Complete the given trip and compute the bill
        Bill resultingBill = null;
        try {
            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());
            resultingBill = currentTrip.endTrip(selectedStation.getStationId());
            tripRepository.save(currentTrip);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());

            safelyCreateAndNotifyEvent(EntityType.TRIP, tripId.value(),
                    "Trip completed", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Trip ended successfully");
        } catch (Exception e) {
            logger.warn("Unable to end trip", e);
            throw new RuntimeException("Failed to end trip during return", e);
        }

        // Persist the resulting bill
        try {
            resultingBill = billRepository.save(resultingBill);
            logger.info("Bill assigned and saved successfully");

            sendCompleteBillToOperators(resultingBill, currentTrip);
        } catch (Exception e) {
            logger.warn("New Bill unable to be created", e);
            throw new RuntimeException("Failed to save bill during return", e);
        }

        Map<String, Object> tripData = new HashMap<>();
        tripData.put("tripId", currentTrip.getTripId().value());
        tripData.put("userId", userId.value());
        tripData.put("bikeId", bikeId.value());
        tripData.put("startStationId", currentTrip.getStartStationId().value());
        tripData.put("endStationId", stationId.value());
        tripData.put("startTime", currentTrip.getStartTime() != null ? currentTrip.getStartTime().toString() : null);
        tripData.put("endTime", currentTrip.getEndTime() != null ? currentTrip.getEndTime().toString() : null);
        tripData.put("bikeType", selectedBike.getBikeType().name());
        tripData.put("status", currentTrip.getStatus().name());
        tripData.put("billId", resultingBill != null ? resultingBill.getBillId().value() : null);
        tripData.put("billCost", resultingBill != null ? resultingBill.getCost() : null);
        sseStationObserver.sendTripUpdate(tripData);

        logger.info("Bike return completed successfully!");

        // Fetch station names
        Station startStation = stationRepository.findById(currentTrip.getStartStationId())
                .orElseThrow(() -> new RuntimeException(
                        "Start station not found with ID: " + currentTrip.getStartStationId().value()));
        Station endStation = selectedStation; // We already have the end station from above

        Map<String, Object> response = new HashMap<>();
        response.put("resultingTrip", currentTrip);
        response.put("resultingBill", resultingBill);
        response.put("startStationName", startStation.getStationName());
        response.put("endStationName", endStation.getStationName());
        response.put("pricingStrategy", currentTrip.getPricingStrategy());
        return response;
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

    private void sendCompleteBillToOperators(Bill bill, Trip trip) {
        try {
            // Get user information
            User user = userRepository.findById(bill.getUserId())
                    .orElse(null);
            String userEmail = user != null ? user.getEmail() : "Unknown";
            String userFullName = user != null ? user.getFullName() : "Unknown";

            // Get station information
            Station startStation = stationRepository.findById(trip.getStartStationId())
                    .orElse(null);
            Station endStation = stationRepository.findById(trip.getEndStationId())
                    .orElse(null);

            String startStationName = startStation != null ? startStation.getStationName() : "Unknown";
            String endStationName = endStation != null ? endStation.getStationName() : "Unknown";

            // Create bill data to match ryan/nicole's frontend
            Map<String, Object> billData = new HashMap<>();

            billData.put("billId", bill.getBillId().value());
            billData.put("tripId", trip.getTripId().value());
            billData.put("userId", bill.getUserId().value());
            billData.put("userEmail", userEmail);
            billData.put("userFullName", userFullName);
            billData.put("bikeId", trip.getBikeId().value());
            billData.put("startStationName", startStationName);
            billData.put("endStationName", endStationName);
            billData.put("startTime", trip.getStartTime().toString());
            billData.put("endTime", trip.getEndTime() != null ? trip.getEndTime().toString() : null);
            billData.put("durationMinutes", Math.round(trip.calculateDurationInMinutes()));
            billData.put("billStatus", bill.getStatus().name());
            billData.put("pricingStrategy",
                    trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPricingTypeName()
                            : "Standard Bike Pricing");
            billData.put("baseFare", trip.getPricingStrategy() != null ? trip.getPricingStrategy().getBaseFee() : 0.0);
            billData.put("perMinuteRate",
                    trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPerMinuteRate() : 0.0);
            billData.put("totalAmount", bill.getCost());

            logger.debug("Sending operator bill update: {}", billData);

            // Send via sse for operators
            sseStationObserver.sendOperatorBillUpdate(billData);

        } catch (Exception e) {
            logger.error("Error sending complete bill data to operators for bill ID: {}", bill.getBillId().value(), e);
        }
    }

}