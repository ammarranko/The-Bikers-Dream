package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.ReservationStatus;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BikeRepository bikeRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoyaltyTierService loyaltyTierService;
           
    @InjectMocks
    private ReservationService reservationService;

    // ========== Tests for checkActiveReservation ==========

    /**
     * Test ReservationService.checkActiveReservation when an active reservation exists for the user.
     * Should return the active reservation.
     */
    @Test
    void checkActiveReservationTest_ActiveReservationExists() {
        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        StationId stationId = new StationId(101L);

        Reservation activeReservation = new Reservation(bikeId, stationId, userId, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis() + 15000));
        when(reservationRepository.checkActiveReservationByUserId(userId)).thenReturn(Optional.of(activeReservation));

        Reservation response = reservationService.checkActiveReservation(userId);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(activeReservation);

        verify(reservationRepository).checkActiveReservationByUserId(userId);
    }
    // ========== Tests for createReservation ==========

    /**
     * Test ReservationService.createReservation() for a successful reservation creation.
     * Should return the created reservation.
     */
    @Test
    void createReservationTest_Successful() {
        Station testStation = mock(Station.class);

        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        StationId stationId = new StationId(101L);

        Bike testBike = new Bike (bikeId, null, BikeStatus.AVAILABLE, null, null);
        User testUser = mock(User.class);

        when(reservationRepository.checkActiveReservationByUserId(userId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(new Reservation(bikeId, stationId, userId, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis() + 15000))));
        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));
        when(stationRepository.findById(stationId)).thenReturn(Optional.of(testStation));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(loyaltyTierService.updateUserTier(testUser)).thenReturn(false);
        when(testStation.getStationId()).thenReturn(stationId);

        Reservation response = reservationService.createReservation(bikeId, stationId, userId);

        assertThat(response).isNotNull();
        assertThat(response.getBikeId()).isEqualTo(bikeId);
        assertThat(response.getStartStationId()).isEqualTo(stationId);
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getReservedAt()).isNotNull();
        assertThat(response.getExpiresAt()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.ACTIVE);

        assertThat(testBike.getStatus()).isEqualTo(BikeStatus.RESERVED);

        verify(bikeRepository).findById(bikeId);
        verify(stationRepository).findById(stationId);
        verify(bikeRepository).save(testBike);
        verify(reservationRepository).save(any(Reservation.class));
    }

    // ========== Tests for cancelReservation ==========

    /**
     * Test ReservationService.cancelReservation() for a successful reservation cancellation.
     * Reservation should be marked as CANCELLED and bike should be made AVAILABLE.
     */

    @Test
    void cancelReservationTest_Successful() {
        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        StationId stationId = new StationId(101L);
        ReservationId reservationId = new ReservationId(100L);

        Bike testBike = new Bike (bikeId, null, BikeStatus.RESERVED, null, null);

        Reservation testReservation = new Reservation(reservationId, bikeId, stationId,
                userId, new java.sql.Timestamp(System.currentTimeMillis()),
                new java.sql.Timestamp(System.currentTimeMillis() + 15000));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));

        reservationService.cancelReservation(reservationId);

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(testReservation);
        verify(bikeRepository).findById(bikeId);
        verify(bikeRepository).save(testBike);

        assertThat(testReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(testBike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
    }

    // ========== Tests for expireReservation ==========
    /**
     * Test ReservationService.expireReservation() for a successful reservation expiration.
     * Should mark the reservation as EXPIRED and set the bike status to AVAILABLE.
     */
    @Test
    void expireReservationTest_Successful() {
        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        StationId stationId = new StationId(101L);
        ReservationId reservationId = new ReservationId(100L);

        Bike testBike = new Bike(bikeId, null, BikeStatus.RESERVED, null, null);
        Reservation testReservation = new Reservation(reservationId, bikeId, stationId,
                userId, null,
                null);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));
        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));

        UserId response = reservationService.expireReservation(reservationId);

        assertThat(response).isNotNull();
        assertThat(testReservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(testBike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(testReservation);
        verify(bikeRepository).findById(bikeId);
        verify(bikeRepository).save(testBike);
    }

    // ========== Tests for completeReservation ==========
    /**
     * Test ReservationService.completeReservation() for a successful reservation completion.
     * Should mark the reservation as COMPLETED
     */
    @Test
    void completeReservationTest_Successful() {
        UserId userId = new UserId(123L);
        BikeId bikeId = new BikeId(456L);
        StationId stationId = new StationId(101L);
        ReservationId reservationId = new ReservationId(100L);

        Bike testBike = mock(Bike.class);

        Reservation testReservation = new Reservation(reservationId, bikeId, stationId,
                userId, new java.sql.Timestamp(System.currentTimeMillis()),
                new java.sql.Timestamp(System.currentTimeMillis() + 15000));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation));

        reservationService.completeReservation(reservationId);

        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).save(testReservation);

        assertThat(testReservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }
}
