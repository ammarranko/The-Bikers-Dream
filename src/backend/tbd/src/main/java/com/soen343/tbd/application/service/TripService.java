package com.soen343.tbd.application.service;

import java.util.HashMap;

import java.util.Map;

import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.pricing.EBikePricing;
import com.soen343.tbd.domain.model.pricing.StandardBikePricing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserService userService;
    private final FlexMoneyService flexMoneyService;
    private final BillingService billingService;

    public TripService(BillRepository billRepository, 
            TripRepository tripRepository, 
            BikeRepository bikeRepository,
            DockRepository dockRepository, 
            StationRepository stationRepository, 
            StationSubject stationPublisher,
            StationService stationService, 
            EventService eventService, 
            UserService userService, 
            FlexMoneyService flexMoneyService,
            BillingService billingService
        ) {
        this.billRepository = billRepository;
        this.tripRepository = tripRepository;
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
        this.stationPublisher = stationPublisher;
        this.stationService = stationService;
        this.eventService = eventService;
        this.flexMoneyService = flexMoneyService;
        this.billingService = billingService;
        this.userService = userService;
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

        logger.info("Bike rental completed successfully!");
        return newTrip;
    }

    @Transactional
    public Map<String, Object> returnBikeService(TripId tripId, BikeId bikeId, DockId dockId, UserId userId, StationId stationId) {
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
        double flexMoneyUsed = 0.0;
        double discountRate = 0.0;
        try {
            discountRate = userService.getUserById(userId).getCurrentDiscount();

            EntityStatus previousStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());
            resultingBill = currentTrip.endTrip(selectedStation.getStationId(), discountRate);
            
            // Round the bill to 2 decimal places before applying FlexMoney to avoid precision issues
            double roundedCost = Math.round(resultingBill.getDiscountedCost() * 100.0) / 100.0;
            resultingBill.setDiscountedCost(roundedCost);

            double costBeforeFlex = resultingBill.getDiscountedCost();
            resultingBill = billingService.applyFlexMoney(resultingBill, userId);
            flexMoneyUsed = costBeforeFlex - resultingBill.getDiscountedCost();
            
            tripRepository.save(currentTrip);
            EntityStatus newStatus = EntityStatus.fromSpecificStatus(currentTrip.getStatus());

            safelyCreateAndNotifyEvent(EntityType.TRIP, tripId.value(),
                    "Trip completed", previousStatus, newStatus,
                    "User_" + userId.value());

            logger.info("Trip ended successfully, discount rate applied: {}", userService.getUserById(userId).getCurrentDiscount());
        } catch (Exception e) {
            logger.warn("Unable to end trip", e);
            throw new RuntimeException("Failed to end trip during return", e);
        }

        // Persist the resulting bill
        try {
            Double regularCost = resultingBill.getRegularCost();

            resultingBill = billRepository.save(resultingBill);
            
            // Restore the value after save (since mapper ignores regularCost when loading from DB)
            resultingBill.setRegularCost(regularCost);
            resultingBill.setFlexMoneyUsed(flexMoneyUsed);
            resultingBill.setLoyaltyDiscount(discountRate);

            logger.info("Bill assigned and saved successfully");
        } catch (Exception e) {
            logger.warn("New Bill unable to be created", e);
            throw new RuntimeException("Failed to save bill during return", e);
        }

        // check for flex money eligibility (after bill so it doesn't get applied to the trip immediately)
        Integer flexMoneyEarned = 0;
        try {
            flexMoneyEarned = flexMoneyService.addFlexMoneyIfEligible(userId, selectedStation, currentTrip.calculateDurationInMinutes());
            if (flexMoneyEarned > 0) {
                logger.info("User earned {} flex money points for returning at a low-fullness station", flexMoneyEarned);
                resultingBill.setFlexMoneyEarned(flexMoneyEarned);
            } else {
                logger.info("User did not earn any flex money points for this trip");
            }
        } catch (Exception e) {
            logger.warn("Failed giving flex money to user: {}", userId.value(), e);
        }
        

        logger.info("Bike return completed successfully!");

        // Fetch station names
        Station startStation = stationRepository.findById(currentTrip.getStartStationId())
                .orElseThrow(() -> new RuntimeException("Start station not found with ID: " + currentTrip.getStartStationId().value()));
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
}