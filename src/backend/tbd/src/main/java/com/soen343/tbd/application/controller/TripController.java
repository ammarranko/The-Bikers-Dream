package com.soen343.tbd.application.controller;

import java.util.Map;

import com.soen343.tbd.application.service.LoyaltyTierService;
import com.soen343.tbd.domain.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.soen343.tbd.application.dto.CheckRentalRequest;
import com.soen343.tbd.application.dto.CheckRentalResponse;
import com.soen343.tbd.application.dto.RentRequest;
import com.soen343.tbd.application.dto.RentResponse;
import com.soen343.tbd.application.dto.ReturnRequest;
import com.soen343.tbd.application.dto.ReturnResponse;
import com.soen343.tbd.application.service.TripService;
import com.soen343.tbd.application.service.UserService;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;
import com.soen343.tbd.application.exception.StationFullException;

@RestController
@RequestMapping("api/trips")
public class TripController {
    private static final Logger logger = LoggerFactory.getLogger(TripController.class);

    @Autowired
    private final TripService tripService;

    @Autowired
    private final UserService userService;

    private final LoyaltyTierService loyaltyTierService;

    public TripController(TripService tripService, UserService userService, LoyaltyTierService loyaltyTierService){
        this.tripService = tripService;
        this.userService = userService;
        this.loyaltyTierService = loyaltyTierService;
    }

    @PostMapping("/rent")
    public ResponseEntity<RentResponse> rentBike(@RequestBody RentRequest request) {
        logger.info("Received bike rental request: BikeId-{}, DockId-{}, User Email-{}, StationId-{}", request.getBikeId(), 
            request.getDockId(), request.getUserEmail(), request.getStationId());
        
        BikeId bId = new BikeId(request.getBikeId());
        DockId dId = new DockId(request.getDockId());
        UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
        StationId sId = new StationId(request.getStationId());

        try {
            Trip newTrip = tripService.rentBikeService(bId, dId, uId, sId);

            logger.info("Successfully Rented Bike with BikeId: {}, TripId: {}", request.getBikeId(), newTrip.getTripId());
            return ResponseEntity.ok(new RentResponse(newTrip.getTripId()));
        } catch (Exception e) {
            logger.warn("Bike unable to be rented, BikeId: {}", request.getBikeId());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/checkRental")
    public ResponseEntity<CheckRentalResponse> checkBikeRental(@RequestBody CheckRentalRequest request) {
        logger.info("Received check rental request: User Email-{}", request.getUserEmail());    
        
        UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();

        try {
            Trip existingTrip = tripService.checkBikeRentalService(uId);

            logger.info("Successfully Checked for Bike Rentals, UserId: {}", uId);

            if (existingTrip == null){
                logger.info("Existing Trip is null, returning null ResponseEntity...");
                throw new RuntimeException();
            }
            
            // Return a DTO object with the values
            CheckRentalResponse bikeRentalDTO = new CheckRentalResponse(existingTrip.getTripId(), uId, 
                true, existingTrip.getBikeId());

            logger.info("Existing trip object found: TripId: {}, BikeId: {}", existingTrip.getTripId(), existingTrip.getBikeId());

            return ResponseEntity.ok(bikeRentalDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new CheckRentalResponse(null, null, 
                false, null));
        } catch (Exception e) {
            logger.warn("Bike rental check unable to be verified, UserId: {}", uId);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/return")
    public ResponseEntity<ReturnResponse> returnBike(@RequestBody ReturnRequest request) {
        logger.info("Received bike rental return request: TripId-{}, BikeId-{}, DockId-{}, User Email-{}, StationId-{}", 
            request.getTripId(), request.getBikeId(), request.getDockId(), request.getUserEmail(), request.getStationId());
        
        TripId tId = new TripId(request.getTripId());
        BikeId bId = new BikeId(request.getBikeId());
        DockId dId = new DockId(request.getDockId());
        UserId uId = userService.getUserWithEmail(request.getUserEmail()).getUserId();
        StationId sId = new StationId(request.getStationId());

        try {

            Map<String, Object> serviceResponse = tripService.returnBikeService(tId, bId, dId, uId, sId);

            // Update user's loyalty tier after completing trip (before creating response)
            User user = userService.getUserById(uId);
            loyaltyTierService.updateUserTier(user);
            logger.info("Updated loyalty tier for user: {}", user.getEmail());

            // Create response with updated tier
            ReturnResponse returnResponse = createReturnResponse(serviceResponse);

            logger.info("Successfully Returned Bike with BikeId: {}", request.getBikeId());
            return ResponseEntity.ok(returnResponse);
        } catch (StationFullException e) {
            logger.warn("Failed to return bike. Station {} is full.", request.getStationId());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.warn("Bike unable to be returned, BikeId: {}", request.getBikeId());
            return ResponseEntity.notFound().build();
        }
    }

    private ReturnResponse createReturnResponse(Map<String, Object> response) {
        // Extract data from the response map
        Trip trip = (Trip) response.get("resultingTrip");
        Bill bill = (Bill) response.get("resultingBill");
        String startStationName = (String) response.get("startStationName");
        String endStationName = (String) response.get("endStationName");
        PricingStrategy pricingStrategy = (PricingStrategy) response.get("pricingStrategy");

        // Use Trip's calculateDurationInMinutes method
        Double durationMinutes = trip.calculateDurationInMinutes();

        User user = userService.getUserById(trip.getUserId());
        String userFullName = user.getFullName();
        String userEmail = user.getEmail();
        String userTier= user.getTierType().name();
        Integer flexMoneyBalance = user.getFlexMoney();

        return new ReturnResponse(
                trip.getTripId().value(),
                trip.getBikeId().value(),
                trip.getUserId().value(),
                userFullName,
                userEmail,
                startStationName,
                endStationName,
                trip.getStartTime(),
                trip.getEndTime(),
                durationMinutes,
                bill.getBillId().value(),
                pricingStrategy.getPricingTypeName(),
                pricingStrategy.getBaseFee(),
                pricingStrategy.getPerMinuteRate(),
                userTier,
                bill.getRegularCost(),
                bill.getDiscountedCost(),
                bill.getFlexMoneyEarned(),
                bill.getFlexMoneyUsed(),
                bill.getLoyaltyDiscount(),
                flexMoneyBalance
        );
    }
}
