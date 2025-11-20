package com.soen343.tbd.integration;

import com.soen343.tbd.application.service.ReservationService;
import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.*;
import com.soen343.tbd.application.observer.StationSubject;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import org.mockito.Mockito;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.enums.EntityStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@Import(ReservationExpiryScenarioIT.TestConfig.class)
public class ReservationExpiryScenarioIT {
    @Autowired
    private ReservationService reservationService;

    // === Repositories to inspect DB state

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DockRepository dockRepository;

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private StationRepository stationRepository;

    // StationSubject mock provided by TestConfig and injected into app context
    @Autowired
    private StationSubject stationPublisher;

    @Test
    void reservationExpires_BikeBecomesAvailable() {

        // Create initial entities
        BikeId bikeId = null;
        DockId dockId = null;
        StationId stationId = null;

        //Create test rider:
        User rider = ITInitializer.initializeRider();
        userRepository.save(rider);
        rider = userRepository.findByEmail("IT@email.com").orElseThrow();

        // Create test dock:
        Dock dock = ITInitializer.initializeDock(dockId, null);
        dock.setStatus(DockStatus.OCCUPIED);
        dock = dockRepository.save(dock);

        //Create test bike:
        Bike bike = ITInitializer.initializeBike(bikeId, dock.getDockId());
        bike = bikeRepository.save(bike);

        // Create test station:
        Station station = ITInitializer.initializeStation(stationId, "Station", java.util.List.of(dock));
        station = stationRepository.save(station);

        // Link dock to station
        dock.setStationId(station.getStationId());
        dockRepository.save(dock);

        // Create a reservation on the bike
        Reservation reservation = reservationService.createReservation(
                bike.getBikeId(),
                station.getStationId(),
                rider.getUserId()
        );

        // Sanity checks before test actions
        assert(userRepository.findById(rider.getUserId()).isPresent());
        assert(bikeRepository.findById(bike.getBikeId()).isPresent());
        assert(dockRepository.findById(dock.getDockId()).isPresent());
        assert(stationRepository.findById(station.getStationId()).isPresent());
        assert(reservationRepository.findById(reservation.getReservationId()).isPresent());
        assert(bikeRepository.findById(bike.getBikeId()).get().getStatus().toString().equals("RESERVED"));

        reservationRepository.save(reservation);

        // Capture final ids to use inside lambda verifications (must be effectively final)
        final Long reservationIdVal = reservation.getReservationId().value();
        final Long bikeIdVal = bike.getBikeId().value();

        // --------- Act: simulate hold time elapsing -----------
        reservationService.expireReservation(reservation.getReservationId());

        // --------- Assert -----------

        Reservation expiredReservation = reservationRepository.findById(reservation.getReservationId()).orElseThrow();
        Bike bikeAfterExpiry = bikeRepository.findById(bike.getBikeId()).orElseThrow();

        // Reservation status should be EXPIRED
        assertThat(expiredReservation.getStatus().toString()).isEqualTo("EXPIRED");

        // Bike status should be AVAILABLE
        assertThat(bikeAfterExpiry.getStatus().toString()).isEqualTo("AVAILABLE");

        // Verify reservation-expired EventDTO was sent
        verify(stationPublisher).notifyOperatorEvent(argThat((EventDTO dto) ->
                dto != null &&
                        EntityType.RESERVATION.name().equals(dto.getEntityType()) &&
                        reservationIdVal.equals(dto.getEntityId()) &&
                        EntityStatus.EXPIRED.name().equals(dto.getNewState()) &&
                        EntityStatus.RES_ACTIVE.name().equals(dto.getPreviousState()) &&
                        "Reservation expired".equals(dto.getMetadata())
        ));

        // Verify bike-available EventDTO was sent (previousState reserved -> available)
        verify(stationPublisher).notifyOperatorEvent(argThat((EventDTO dto) ->
                dto != null &&
                        EntityType.BIKE.name().equals(dto.getEntityType()) &&
                        bikeIdVal.equals(dto.getEntityId()) &&
                        EntityStatus.AVAILABLE.name().equals(dto.getNewState()) &&
                        EntityStatus.RESERVED.name().equals(dto.getPreviousState()) &&
                        "Bike became available after reservation expiry".equals(dto.getMetadata())
        ));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public StationSubject stationSubject() {
            return Mockito.mock(StationSubject.class);
        }
    }
}
