package com.soen343.tbd.integration;

import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.application.observer.StationSubject;
import com.soen343.tbd.application.service.TripService;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@Import(RebalancingScenarioIT.TestConfig.class)
public class RebalancingScenarioIT {
    @Autowired
    private TripService tripService;

    // === Repositories to inspect DB state
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DockRepository dockRepository;

    @Autowired
    private BikeRepository bikeRepository;

    // StationSubject mock provided by TestConfig and injected into app context
    @Autowired
    private StationSubject stationPublisher;

    @Test
    void stationIsEmptied_AlertToRebalanceSentTest() {
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
        station.setStationAvailability(StationAvailability.FULL);
        station = stationRepository.save(station);

        // Link dock to station
        dock.setStationId(station.getStationId());
        dockRepository.save(dock);

        // Capture final ids to use inside lambda verifications (must be effectively final)
        final Long finalStationId = station.getStationId().value();

        // --------- Act -----------
        tripService.rentBikeService(bike.getBikeId(), dock.getDockId(), rider.getUserId(), station.getStationId());

        // --------- Assert ---------
        // Verify that at least one invocation to notifyOperatorEvent contained the expected Station EMPTY DTO
        verify(stationPublisher, times(1)).notifyOperatorEvent(argThat((EventDTO dto) ->
                dto != null &&
                        EntityType.STATION.name().equals(dto.getEntityType()) &&
                        finalStationId.equals(dto.getEntityId()) &&
                        EntityStatus.STATION_EMPTY.name().equals(dto.getNewState()) &&
                        "ALERT! Station is empty! Rebalance Required!".equals(dto.getMetadata())
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
