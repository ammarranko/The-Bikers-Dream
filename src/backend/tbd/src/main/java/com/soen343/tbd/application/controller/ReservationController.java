package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.dto.ReservationRequest;
import com.soen343.tbd.application.dto.ReservationResponse;
import com.soen343.tbd.application.service.ReservationService;
import com.soen343.tbd.application.service.UserService;
import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.ids.ReservationId;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/reservations")
@CrossOrigin(origins = "http://localhost:3000")

public class ReservationController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        logger.info("Received reservation request: BikeId={}, StationId={}, UserEmail={}", 
                    request.getBikeId(), request.getStationId(), request.getUserEmail());

        try {
            UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
            BikeId bId = new BikeId(request.getBikeId());
            StationId sId = new StationId(request.getStationId());

            Reservation newReservation = reservationService.createReservation(bId, sId, uId);

            logger.info("Reservation created successfully: ReservationId={}", newReservation.getReservationId().value());
            return ResponseEntity.ok(new ReservationResponse("Reservation created successfully", newReservation.getReservationId().value()));

        } catch (RuntimeException e) {
            logger.warn("Reservation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ReservationResponse("Reservation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/check")
    public ResponseEntity<ReservationResponse> checkActiveReservation(@RequestBody ReservationRequest request) {
        logger.info("Checking active reservation for UserEmail={}", request.getUserEmail());

        try {
            UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
            Reservation activeReservation = reservationService.checkActiveReservation(uId);

            if (activeReservation != null) {
                logger.info("Active reservation found: ReservationId={}", activeReservation.getReservationId().value());
                return ResponseEntity.ok(new ReservationResponse("Active reservation found", activeReservation.getReservationId().value()));
            } else {
                logger.info("No active reservation found for UserEmail={}", request.getUserEmail());
                return ResponseEntity.ok(new ReservationResponse("No active reservation found"));
            }

        } catch (Exception e) {
            logger.warn("Error checking reservation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ReservationResponse("Error checking reservation"));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@RequestBody ReservationRequest request) {
        //to delete line 
    Long reservationLong = request.getReservationId();
    if (reservationLong == null) {
        return ResponseEntity.badRequest()
                .body(new ReservationResponse("ReservationId is required"));
    }

    ReservationId reservationId = new ReservationId(reservationLong);

    logger.info("Received cancel reservation request: ReservationId={}", reservationId.value());

         try {
        reservationService.cancelReservation(reservationId);
        logger.info("Reservation {} cancelled successfully", reservationId.value());
        return ResponseEntity.ok(new ReservationResponse("Reservation cancelled successfully"));

        } catch (RuntimeException e) {
            logger.warn("Cancel reservation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
            .body(new ReservationResponse("Cancel failed: " + e.getMessage()));
        }
    }
}
