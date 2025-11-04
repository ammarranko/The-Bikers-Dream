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
import org.springframework.http.HttpStatus;


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

    // --- Create Reservation ---
    @PostMapping("/create")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest request) {
        try {
            logger.info("Received reservation request: BikeId={}, StationId={}, UserEmail={}",
                    request.getBikeId(), request.getStationId(), request.getUserEmail());

            UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
            BikeId bId = new BikeId(request.getBikeId());
            StationId sId = new StationId(request.getStationId());

            Reservation newReservation = reservationService.createReservation(bId, sId, uId);

            logger.info("Reservation created successfully: ReservationId={}, BikeId={}",
                    newReservation.getReservationId().value(), bId.value());

            return ResponseEntity.ok(
                    new ReservationResponse(
                            "Reservation created successfully",
                            newReservation.getReservationId().value(),
                            newReservation.getBikeId().value(),
                            newReservation.getStartStationId().value(),
                            newReservation.getExpiresAt().toLocalDateTime()  // convert Timestamp -> LocalDateTime
                    )
            );
            } catch (Exception e) {
                logger.warn("Reservation failed", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                         .body(new ReservationResponse("Reservation failed: " + e.getMessage()));
            }

    }

    // --- Check Active Reservation ---
    @PostMapping("/check")
    public ResponseEntity<ReservationResponse> checkActiveReservation(@RequestBody ReservationRequest request) {
        try {
            logger.info("Checking active reservation for UserEmail={}", request.getUserEmail());

            UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
            Reservation activeReservation = reservationService.checkActiveReservation(uId);

            if (activeReservation != null) {
                logger.info("Active reservation found: ReservationId={}", activeReservation.getReservationId().value());
                return ResponseEntity.ok(
                        new ReservationResponse(
                                true,
                                activeReservation.getBikeId().value(),
                                activeReservation.getStartStationId().value(),
                                activeReservation.getExpiresAt().toLocalDateTime() 
                        )
                );
            } else {
                logger.info("No active reservation found for UserEmail={}", request.getUserEmail());
                return ResponseEntity.ok(new ReservationResponse(false));
            }
        } catch (Exception e) {
            logger.warn("Reservation check failed for UserId={}: {}", request.getUserEmail(), e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // --- End Reservation (Cancel or Expire) ---
    @PostMapping("/end")
    public ResponseEntity<ReservationResponse> endReservation(
            @RequestBody ReservationRequest request,
            @RequestParam(name = "type", defaultValue = "CANCEL") String type) {
        
        if (request.getReservationId() == null) {
            return ResponseEntity.badRequest()
                    .body(new ReservationResponse("ReservationId is required"));
        }

        ReservationId reservationId = new ReservationId(request.getReservationId());
        boolean isExpired = "EXPIRE".equalsIgnoreCase(type);
        
        logger.info("Received {} reservation request: ReservationId={}", 
            isExpired ? "expire" : "cancel", 
            reservationId.value());

        try {
            if (isExpired) {
                reservationService.expireReservation(reservationId);
                logger.info("Reservation {} expired", reservationId.value());
                return ResponseEntity.ok(new ReservationResponse("Reservation expired"));
            } else {
                reservationService.cancelReservation(reservationId);
                logger.info("Reservation {} cancelled successfully", reservationId.value());
                return ResponseEntity.ok(new ReservationResponse("Reservation cancelled successfully"));
            }
        } catch (Exception e) {
            logger.warn("{} reservation failed: {}", 
                isExpired ? "Expire" : "Cancel",
                e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
