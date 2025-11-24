package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.BillRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HistoryServiceTest {
    @Mock
    private TripRepository tripRepository;
    @Mock
    private BikeRepository bikeRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HistoryService historyService;

    @Mock
    private Trip testTrip;
    @Mock
    private Bike testBike;
    @Mock
    private Bill testBill;
    @Mock
    private User testUser;

    @Test
    void getTripByTripIdAndEmailTest() {
        Long tripId = 1L;
        String email = "test@example.com";
        TripId domainTripId = new TripId(tripId);

        when(tripRepository.findByTripIdAndEmail(domainTripId, email)).thenReturn(Optional.of(testTrip));

        Optional<Trip> result = historyService.getTripByTripIdAndEmail(tripId, email);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testTrip);

        verify(tripRepository).findByTripIdAndEmail(domainTripId, email);
    }

    @Test
    void getAllTripsByEmailTest() {
        String email = "test@example.com";

        Trip testTrip2 = mock(Trip.class);

        when(tripRepository.findTripByEmail(email)).thenReturn(List.of(testTrip, testTrip2));

        List<Trip> result = historyService.getAllTripsByEmail(email);

        assertThat(result).containsExactly(testTrip, testTrip2);

        verify(tripRepository).findTripByEmail(email);
    }

    @Test
    void getAllTripsTest() {
        Trip testTrip2 = mock(Trip.class);

        when(tripRepository.findAllTrips()).thenReturn(List.of(testTrip, testTrip2));

        List<Trip> result = historyService.getAllTrips();

        assertThat(result).containsExactly(testTrip, testTrip2);

        verify(tripRepository).findAllTrips();
    }

    @Test
    void testGetBikeById() {
        Long bikeId = 2L;
        BikeId domainBikeId = new BikeId(bikeId);

        when(bikeRepository.findById(domainBikeId)).thenReturn(Optional.of(testBike));

        Optional<Bike> result = historyService.getBikeById(bikeId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBike);

        verify(bikeRepository).findById(domainBikeId);
    }

    @Test
    void testGetBillById() {
        Long billId = 3L;
        BillId domainBillId = new BillId(billId);

        when(billRepository.findById(domainBillId)).thenReturn(Optional.of(testBill));

        Optional<Bill> result = historyService.getBillById(billId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBill);

        verify(billRepository).findById(domainBillId);
    }

    @Test
    void testFindUserById() {
        Long userId = 4L;
        UserId domainUserId = new UserId(userId);

        when(userRepository.findById(domainUserId)).thenReturn(Optional.of(testUser));

        Optional<User> result = historyService.findUserById(userId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);

        verify(userRepository).findById(domainUserId);
    }
}
