package com.soen343.tbd.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.application.observer.StationSubject;
import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.enums.ReservationStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.ids.ReservationId;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Station;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final StationService stationService;
    private final StationSubject stationPublisher;
    private final EventService eventService;

    public ReservationService(ReservationRepository reservationRepository,
            BikeRepository bikeRepository,
            UserRepository userRepository,
            StationRepository stationRepository,
            StationSubject stationPublisher,
            StationService stationService,
            EventService eventService) {
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.stationService = stationService;
        this.stationPublisher = stationPublisher;
        this.eventService = eventService;
    }

    // -------------------------
    // CREATE RESERVATION
    // -------------------------
    @Transactional
    public Reservation createReservation(BikeId bikeId, StationId stationId, UserId userId) {
        logger.info("Starting reservation creation for BikeId={}, StationId={}, UserId={}",
                bikeId.value(), stationId.value(), userId.value());

        try {
            // Check if user already has an active reservation
            if (reservationRepository.checkActiveReservationByUserId(userId).isPresent()) {
                throw new RuntimeException("User already has an active reservation");
            }
        } catch (Exception e) {
            logger.error("New reservation unable to be created...", e.getMessage());
            throw new RuntimeException("Failed to create reservation", e);
        }

        // Fetch entities
        Bike selectedBike = bikeRepository.findById(bikeId)
                .orElseThrow(() -> new RuntimeException("Bike not found: " + bikeId.value()));
        Station selectedStation = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found: " + stationId.value()));

        logger.info("Found bike: {}, station: {}",
                selectedBike.getBikeId().value(),
                selectedStation.getStationId().value());
    
        Reservation newReservation= null;


        // Create and save reservation
        try {
            Timestamp reservedAt = Timestamp.from(Instant.now());
            Timestamp expiresAt = Timestamp.from(Instant.now().plus(15, ChronoUnit.SECONDS));

            // Create reservation using domain constructor
            newReservation = new Reservation(bikeId, stationId, userId, reservedAt, expiresAt);
            newReservation.setStatus(ReservationStatus.ACTIVE);

            // Update bike status to RESERVED
            selectedBike.setStatus(BikeStatus.RESERVED);
            bikeRepository.save(selectedBike);

            reservationRepository.save(newReservation);

            logger.info("Reservation created successfully: ReservationId={}", newReservation.getReservationId());

            // Retrieve saved reservation
            newReservation = reservationRepository.checkActiveReservationByUserId(userId)
                    .orElse(null);
            
            // Create event for reservation creation
            eventService.createEventForEntity(
                EntityType.RESERVATION,
                newReservation.getReservationId().value(),
                "Reservation created",
                EntityStatus.NONE,
                EntityStatus.RES_ACTIVE,
                "User_"+userId.value());
            
            // Create event for bike status change
            eventService.createEventForEntity(
                EntityType.BIKE,
                selectedBike.getBikeId().value(),
                "Bike reserved",
                EntityStatus.AVAILABLE,
                EntityStatus.RESERVED,
                "User_"+userId.value());

            // Notify all observers about station update
            notifyAllUsers(selectedStation.getStationId());
        } catch (Exception e) {
            logger.warn("Reservation creation failed: {}", e.getMessage());
        }

        return newReservation;
    }

    // -------------------------
    // CHECK ACTIVE RESERVATION
    // -------------------------
    @Transactional
    public Reservation checkActiveReservation(UserId userId) {
        logger.info("Checking active reservation for UserId={}", userId.value());

        Optional<Reservation> activeReservationOpt = reservationRepository.checkActiveReservationByUserId(userId);

        if (activeReservationOpt.isPresent()) {
            Reservation currentReservation = activeReservationOpt.get();

            // Expire if past expiry
            if (currentReservation.isExpired()) {
                expireReservation(currentReservation.getReservationId());
                return null;
            }

            logger.info("Active reservation found: ReservationId={}", currentReservation.getReservationId());
            return currentReservation;
        }

        logger.info("No active reservation found for UserId={}", userId.value());
        return null;
    }

    // -------------------------
    // CANCEL RESERVATION
    // -------------------------
    @Transactional
    public void cancelReservation(ReservationId reservationId) {
        logger.info("Attempting to cancel reservation: ReservationId={}", reservationId.value());

        Reservation cancelReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId.value()));

        try {
            cancelReservation.cancel();
            reservationRepository.save(cancelReservation);

            // Update bike status
            Bike bike = bikeRepository.findById(cancelReservation.getBikeId())
                    .orElseThrow(
                            () -> new RuntimeException("Bike not found: " + cancelReservation.getBikeId().value()));
            bike.setStatus(BikeStatus.AVAILABLE);
            bikeRepository.save(bike);
            
            // Create event for reservation cancellation
            eventService.createEventForEntity(
                    EntityType.RESERVATION,
                    cancelReservation.getReservationId().value(),
                    "Reservation cancelled",
                    EntityStatus.RES_ACTIVE,
                    EntityStatus.CANCELLED,
                    "System");
            
            // Create event for bike status change
            eventService.createEventForEntity(
                    EntityType.BIKE,
                    bike.getBikeId().value(),
                    "Bike made available after reservation cancellation",
                    EntityStatus.RESERVED,
                    EntityStatus.AVAILABLE,
                    "System");  

            // Notify
            notifyAllUsers(cancelReservation.getStartStationId());

            logger.info("Reservation {} cancelled successfully", reservationId.value());
        } catch (Exception e) {
            logger.warn("Failed to cancel reservation {}: {}", reservationId.value(), e.getMessage());
        }
    }

    // -------------------------
    // EXPIRE RESERVATION
    // -------------------------
    @Transactional
    public void expireReservation(ReservationId reservationId) {
        Reservation expiredReservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        expiredReservation.expire(); // Domain handles ACTIVE check and expiration
        reservationRepository.save(expiredReservation);

        // Update bike status if expired
        if (expiredReservation.getStatus() == ReservationStatus.EXPIRED) {
            Bike bike = bikeRepository.findById(expiredReservation.getBikeId())
                    .orElseThrow(() -> new RuntimeException("Bike not found"));
            bike.setStatus(BikeStatus.AVAILABLE);
            bikeRepository.save(bike);

            // Create event for reservation expiration
            eventService.createEventForEntity(
                    EntityType.RESERVATION,
                    expiredReservation.getReservationId().value(),
                    "Reservation expired",
                    EntityStatus.RES_ACTIVE,
                    EntityStatus.EXPIRED,
                    "System");

            // Create event for bike status change
            eventService.createEventForEntity(
                    EntityType.BIKE,
                    bike.getBikeId().value(),
                    "Bike made available after reservation expiration",
                    EntityStatus.RESERVED,
                    EntityStatus.AVAILABLE,
                    "System");

            // Notify
            notifyAllUsers(expiredReservation.getStartStationId());

            logger.info("Reservation {} expired, bike set to AVAILABLE", reservationId.value());
        }
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
