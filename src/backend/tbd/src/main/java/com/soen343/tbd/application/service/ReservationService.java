package com.soen343.tbd.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.enums.BikeStatus;
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

    public ReservationService(ReservationRepository reservationRepository,
                              BikeRepository bikeRepository,
                              UserRepository userRepository,
                              StationRepository stationRepository) {
        this.reservationRepository = reservationRepository;
        this.bikeRepository = bikeRepository;
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
    }

    // -------------------------
    // CREATE RESERVATION
    // -------------------------
    @Transactional
    public Reservation createReservation(BikeId bikeId, StationId stationId, UserId userId) {
        logger.info("Starting reservation creation for BikeId={}, StationId={}, UserId={}", 
                    bikeId.value(), stationId.value(), userId.value());

        // Fetch entities
        Bike selectedBike = bikeRepository.findById(bikeId)
            .orElseThrow(() -> new RuntimeException("Bike not found: " + bikeId.value()));
        Station selectedStation = stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Station not found: " + stationId.value()));
        // Optional: check user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId.value()));

        // Check for existing active reservation
        Reservation activeReservation = checkActiveReservation(userId);
        if (activeReservation != null) {
            throw new RuntimeException("User already has an active reservation");
        }

        if (selectedBike.getStatus() != BikeStatus.AVAILABLE) {
            throw new RuntimeException("Bike is not available for reservation");
        }

        // Create and save reservation
        Reservation newReservation = null;
        try {
            Timestamp reservedAt = Timestamp.from(Instant.now());
            Timestamp expiresAt = Timestamp.from(Instant.now().plus(5, ChronoUnit.MINUTES));

            newReservation = new Reservation(null, bikeId, stationId, userId, reservedAt, expiresAt);
            newReservation.setStatus(ReservationStatus.ACTIVE);

            // Update bike status to RESERVED
            selectedBike.setStatus(BikeStatus.RESERVED);
            bikeRepository.save(selectedBike);

            reservationRepository.save(newReservation);
            logger.info("Reservation created successfully: ReservationId={}", newReservation.getReservationId());

            // Retrieve saved reservation
            newReservation = reservationRepository.checkActiveReservationByUserId(userId)
                                .orElse(null);
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
            Reservation res = activeReservationOpt.get();

            // Expire if past expiry
            if (res.getExpiresAt().before(Timestamp.from(Instant.now()))) {
                logger.info("Reservation {} expired, updating status", res.getReservationId());
                expireReservation(res.getReservationId());
                return null;
            }

            logger.info("Active reservation found: ReservationId={}", res.getReservationId());
            return res;
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

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId.value()));

        try {
            reservation.cancel();
            reservationRepository.save(reservation);

            // Update bike status
            Bike bike = bikeRepository.findById(reservation.getBikeId())
                        .orElseThrow(() -> new RuntimeException("Bike not found: " + reservation.getBikeId().value()));
            bike.setStatus(BikeStatus.AVAILABLE);
            bikeRepository.save(bike);

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
        logger.info("Checking expiration for reservation: ReservationId={}", reservationId.value());

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId.value()));

        try {
            if (reservation.getStatus() == ReservationStatus.ACTIVE &&
                reservation.getExpiresAt().before(Timestamp.from(Instant.now()))) {

                reservation.setStatus(ReservationStatus.EXPIRED);

                Bike bike = bikeRepository.findById(reservation.getBikeId())
                                .orElseThrow(() -> new RuntimeException("Bike not found: " + reservation.getBikeId().value()));
                bike.setStatus(BikeStatus.AVAILABLE);
                bikeRepository.save(bike);

                reservationRepository.save(reservation);
                logger.info("Reservation {} expired successfully", reservationId.value());
            }
        } catch (Exception e) {
            logger.warn("Failed to expire reservation {}: {}", reservationId.value(), e.getMessage());
        }
    }
}

