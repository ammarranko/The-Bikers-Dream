package com.soen343.tbd.integration;

import com.soen343.tbd.application.service.BillingService;
import com.soen343.tbd.application.service.ReservationService;
import com.soen343.tbd.application.service.TripService;
import com.soen343.tbd.domain.model.*;
import com.soen343.tbd.domain.model.enums.*;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
public class HappyPathScenarioIT {

    // === Services under test ===

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TripService tripService;

    // === Repositories to inspect DB state

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DockRepository dockRepository;

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private StationRepository stationRepository;

    @Test
    void riderReserves_UnlocksBike_ReturnsBike_BilledSuccessfully() {
        // Create initial entities
        BikeId bikeId = null;
        DockId dockIdAtStationA = null;
        DockId dockIdAtStationB = null;
        StationId stationAId = null;
        StationId stationBId = null;

        //Create test rider:
        User rider = ITInitializer.initializeRider();
        userRepository.save(rider);
        rider = userRepository.findByEmail("IT@email.com").orElseThrow();

        // Create source test dock:
        Dock dockAtStationA = ITInitializer.initializeDock(dockIdAtStationA, null);
        dockAtStationA.setStatus(DockStatus.OCCUPIED);
        dockAtStationA = dockRepository.save(dockAtStationA);

        //Create test bike:
        Bike bike = ITInitializer.initializeBike(bikeId, dockAtStationA.getDockId());
        bike = bikeRepository.save(bike);

        // Create target test dock:
        Dock dockAtStationB = ITInitializer.initializeDock(dockIdAtStationB, null);
        dockAtStationB = dockRepository.save(dockAtStationB);

        // Create source test station:
        Station stationA = ITInitializer.initializeStation(stationAId, "Station A", List.of(dockAtStationA));
        stationA = stationRepository.save(stationA);

        // Create target test station:
        Station stationB = ITInitializer.initializeStation(stationBId, "Station B", List.of(dockAtStationB));
        stationB = stationRepository.save(stationB);

        // Link docks to stations:
        dockAtStationA.setStationId(stationA.getStationId());
        dockAtStationB.setStationId(stationB.getStationId());

        dockRepository.save(dockAtStationA);
        dockRepository.save(dockAtStationB);

        // Sanity checks before test actions
        assert(userRepository.findById(rider.getUserId()).isPresent());
        assert(bikeRepository.findById(bike.getBikeId()).isPresent());
        assert(dockRepository.findById(dockAtStationA.getDockId()).isPresent());
        assert(dockRepository.findById(dockAtStationB.getDockId()).isPresent());
        assert(stationRepository.findById(stationA.getStationId()).isPresent());
        assert(stationRepository.findById(stationB.getStationId()).isPresent());

        bikeId = bike.getBikeId();
        dockIdAtStationA = dockAtStationA.getDockId();
        dockIdAtStationB = dockAtStationB.getDockId();
        stationAId = stationA.getStationId();
        stationBId = stationB.getStationId();


        // ---------- Act ----------
        // Step 1: Rider reserves a bike at Station A
        Reservation reservation = reservationService.createReservation(
                bike.getBikeId(),
                stationA.getStationId(),
                rider.getUserId()
        );

        // Step 2: Rider unlocks the reserved bike at Station A and completes the reservation
        Trip trip = tripService.rentBikeService(bike.getBikeId(), dockAtStationA.getDockId(),
                rider.getUserId(), stationA.getStationId());

        reservationService.completeReservation(reservation.getReservationId());

        // Step 3: Rider returns the bike to Station B
        Map<String, Object> serviceResponse = tripService.returnBikeService(trip.getTripId(), bike.getBikeId(), dockAtStationB.getDockId(),
                rider.getUserId(), stationB.getStationId());

        // ---------- Assert ----------
        // A) Station occupancy
        Station stationAAfter = stationRepository.findById(stationAId).orElseThrow();
        Station stationBAfter = stationRepository.findById(stationBId).orElseThrow();

        // After the ride, Station A has 0 available bikes
        assertThat(stationAAfter.getNumberOfBikesDocked()).isEqualTo(0);

        // After the ride, Station B has 1 available bike
        assertThat(stationBAfter.getNumberOfBikesDocked()).isEqualTo(1);

        // B) Trip state
        Trip tripFromDb = tripRepository.findById(trip.getTripId()).orElseThrow();

        assertThat(tripFromDb.getUserId()).isEqualTo(rider.getUserId());
        assertThat(tripFromDb.getStartStationId()).isEqualTo(stationAId);
        assertThat(tripFromDb.getEndStationId()).isEqualTo(stationBId);
        assertThat(tripFromDb.getStartTime()).isNotNull();
        assertThat(tripFromDb.getEndTime()).isNotNull();
        assertThat(tripFromDb.getStatus()).isEqualTo(TripStatus.COMPLETED);

        // D) Billing
        Bill billFromDb = billRepository.findById(tripFromDb.getBillId())
                .orElseThrow();

        // E) Reservation state
        Reservation reservationFromDb = reservationRepository.findById(reservation.getReservationId())
                .orElseThrow();
        assertThat(reservationFromDb.getStatus()).isEqualTo(ReservationStatus.COMPLETED);


        assertThat(billFromDb.getUserId()).isEqualTo(rider.getUserId());
        assertThat(billFromDb.getTripId()).isEqualTo(tripFromDb.getTripId());
        assertThat(billFromDb.getDiscountedCost()).isGreaterThan(0.0);

        trip = (Trip) serviceResponse.get("resultingTrip");
        Bill bill = (Bill) serviceResponse.get("resultingBill");
        String startStationName = (String) serviceResponse.get("startStationName");
        String endStationName = (String) serviceResponse.get("endStationName");
        PricingStrategy pricingStrategy = (PricingStrategy) serviceResponse.get("pricingStrategy");

        System.out.println("--- Trip and Bill Information ---");
        System.out.println("Trip ID: " + trip.getTripId());
        System.out.println("User ID: " + trip.getUserId());
        System.out.println("Bike ID: " + trip.getBikeId());
        System.out.println("Start Station: " + startStationName);
        System.out.println("End Station: " + endStationName);
        System.out.println("Start Time: " + trip.getStartTime());
        System.out.println("End Time: " + trip.getEndTime());
        System.out.println("Trip Status: " + trip.getStatus());
        System.out.println("Pricing Plan: " + pricingStrategy.getClass().getSimpleName());
        System.out.println("Bill ID: " + bill.getBillId());
        System.out.println("Cost: " + bill.getDiscountedCost());
        System.out.println("-------------------------------");

    }
}
