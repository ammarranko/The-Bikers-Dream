package com.soen343.tbd.application.service;

import com.soen343.tbd.application.observer.StationSubject;
import com.soen343.tbd.domain.model.*;
import com.soen343.tbd.domain.model.enums.*;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.pricing.StandardBikePricing;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private DockRepository dockRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationService stationService;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @Mock
    private StationSubject stationPublisher;

    @InjectMocks
    private TripService tripService;

    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testTrip = mock(Trip.class);
    }

    // ========== Tests for checkBikeRentalService() ==========
    /**
     * Test TripService.checkBikeRentalService when the user has an active rental.
     * Should return the active Trip.
     */
    @Test
    void checkBikeRentalServiceTest_WhenUserHasActiveRental() {
        UserId userId = new UserId(123L);
        when(tripRepository.checkRentalsByUserId(userId)).thenReturn(Optional.of(testTrip));

        Trip response = tripService.checkBikeRentalService(userId);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(testTrip);
    }

    /**
     * Test TripService.checkBikeRentalService when the user has no active rental.
     * Should return null.
     */
    @Test
    void checkBikeRentalServiceTest_WhenUserHasNoActiveRental() {
        UserId userId = new UserId(123L);
        when(tripRepository.checkRentalsByUserId(userId)).thenReturn(Optional.empty());

        Trip response = tripService.checkBikeRentalService(userId);

        assertThat(response).isNull();
    }

    // ========== Tests for rentBikeService() ==========

    /**
     * Test TripService.rentBikeService for a successful bike rental.
     * Should return the newly created Trip with correct values.
     */
    @Test
    void rentBikeServiceTest_SuccessfulRental() {
        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        DockId dockId = new DockId(789L);
        StationId stationId = new StationId(101L);
        TripId tripId = new TripId(100L);

        // Create real entities
        Bike bike = new Bike(bikeId, dockId, BikeStatus.AVAILABLE, BikeType.STANDARD, null);
        Dock dock = new Dock(dockId, stationId, DockStatus.OCCUPIED);
        List<Dock> stationDocks = java.util.Arrays.asList(
                dock,
                new Dock(new DockId(201L), stationId, DockStatus.OCCUPIED),
                new Dock(new DockId(202L), stationId, DockStatus.OCCUPIED)
        );
        Station station = new Station(stationId, "Test Station", StationAvailability.OCCUPIED, StationStatus.ACTIVE, "pos", "addr", 3, 3, stationDocks);

        // Create real Trip
        Trip trip = createTripForTests(tripId, bikeId, userId, stationId);
        trip.setBillId(new BillId(888L));
        trip.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));

        // Mock repository/service methods to return real entities
        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(bike));
        when(dockRepository.findById(dockId)).thenReturn(Optional.of(dock));
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripRepository.checkRentalsByUserId(userId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(trip));

        Trip response = tripService.rentBikeService(bikeId, dockId, userId, stationId);

        // Assert the actual values of the real object
        assertThat(response).isNotNull();
        assertThat(response.getTripId()).isEqualTo(tripId);
        assertThat(response.getStatus()).isEqualTo(TripStatus.ONGOING);
        assertThat(response.getBikeId()).isEqualTo(bikeId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getBillId()).isNotNull();
        assertThat(response.getStartStationId()).isEqualTo(stationId);
        assertThat(response.getStartTime()).isNotNull();
        assertThat(response.getPricingStrategy()).isInstanceOf(StandardBikePricing.class);

        // Assert entity state changes
        assertThat(bike.getStatus()).isEqualTo(BikeStatus.ON_TRIP);
        assertThat(dock.getStatus()).isEqualTo(DockStatus.EMPTY);
        assertThat(station.getNumberOfBikesDocked()).isEqualTo(2); // decremented

        // Verify repository/service interactions
        verify(bikeRepository).findById(bikeId);
        verify(bikeRepository).save(any(Bike.class));
        verify(dockRepository).findById(dockId);
        verify(dockRepository).save(any(Dock.class));
        verify(stationRepository).findById(stationId);
        verify(stationRepository).save(any(Station.class));
        verify(tripRepository).save(any(Trip.class));
    }

    // ========== Tests for returnBikeService() ==========

    /**
     * Test TripService.returnBikeService for a successful bike return.
     * Should return a map with the resulting Trip, Bill, station names, and pricing strategy
     */
    @Test
    void returnBikeServiceTest_SuccessfulReturn_RealEntities() {
        TripId tripId = new TripId(100L);
        BikeId bikeId = new BikeId(456L);
        DockId dockId = new DockId(789L);
        UserId userId = new UserId(123L);
        BillId billId = new BillId(999L);
        StationId startStationId = new StationId(101L);
        StationId endStationId = new StationId(102L);

        // Create real entities with initial state
        Bike bike = new Bike(bikeId, dockId, BikeStatus.RESERVED, BikeType.STANDARD, null);
        Dock dock = new Dock(dockId, endStationId, DockStatus.EMPTY);
        Station startStation = new Station(startStationId, "Test Start Station", StationAvailability.OCCUPIED, StationStatus.ACTIVE, "pos1", "addr1", 10, 5, java.util.Collections.emptyList());

        // Create 3 occupied docks for endStation
        List<Dock> endStationDocks = java.util.Arrays.asList(
            dock,
            new Dock(new DockId(202L), endStationId, DockStatus.OCCUPIED),
            new Dock(new DockId(203L), endStationId, DockStatus.OCCUPIED)
        );
        Station endStation = new Station(endStationId, "Test End Station", StationAvailability.OCCUPIED, StationStatus.ACTIVE, "pos2", "addr2", 5, 2, endStationDocks);

        Trip trip = createTripForTests(tripId, bikeId, userId, startStationId);

        Bill bill = createBillForTests(billId);

        User user = mock(User.class);
        when(user.getCurrentDiscount()).thenReturn(0.1);

        // Mock repository/service methods to return real entities
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(bike));
        when(dockRepository.findById(dockId)).thenReturn(Optional.of(dock));
        when(stationRepository.findById(startStationId)).thenReturn(Optional.of(startStation));
        when(stationRepository.findById(endStationId)).thenReturn(Optional.of(endStation));
        when(userService.getUserById(userId)).thenReturn(user);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        // Call the service
        Map<String, Object> response = tripService.returnBikeService(tripId, bikeId, dockId, userId, endStationId);

        // Assert entity state changes
        assertThat(bike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
        assertThat(bike.getDockId()).isEqualTo(dockId);

        assertThat(dock.getStatus()).isEqualTo(DockStatus.OCCUPIED);

        assertThat(endStation.getNumberOfBikesDocked()).isEqualTo(3); // incremented
        assertThat(response.get("startStationName")).isEqualTo("Test Start Station");
        assertThat(response.get("endStationName")).isEqualTo("Test End Station");

        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(response.get("resultingTrip")).isEqualTo(trip);

        assertThat(response.get("resultingBill")).isEqualTo(bill);
        assertThat(response.get("pricingStrategy")).isInstanceOf(StandardBikePricing.class);

        // Verify repository/service interactions
        verify(tripRepository).findById(tripId);
        verify(bikeRepository).findById(bikeId);
        verify(dockRepository).findById(dockId);
        verify(stationRepository).findById(endStationId);
        verify(stationRepository).findById(startStationId);
        verify(userService, times(2)).getUserById(userId);
        verify(tripRepository, times(2)).save(trip);
        verify(billRepository).save(any(Bill.class));
    }

    private Trip createTripForTests(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId) {
        Trip trip = new Trip(tripId, bikeId, userId, startStationId, new StandardBikePricing());
        return trip;
    }

    private Bill createBillForTests(BillId billId) {
        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setRegularCost(10.0);
        bill.setDiscountedCost(9.0);
        bill.setStatus(BillStatus.PENDING);

        return bill;
    }
}
