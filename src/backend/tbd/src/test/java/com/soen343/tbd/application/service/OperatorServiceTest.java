package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.*;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.application.dto.OperatorRebalanceDTO;
import com.soen343.tbd.application.observer.StationSubject;
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
public class OperatorServiceTest {
    @Mock
    private BikeRepository bikeRepository;
    @Mock
    private DockRepository dockRepository;
    @Mock
    private StationRepository stationRepository;
    @Mock
    private EventService eventService;
    @Mock
    private StationService stationService;
    @Mock
    private StationSubject stationPublisher;
    @InjectMocks
    private OperatorService operatorService;
    @Mock
    private Bike testBike;
    @Mock
    private Dock testDock;
    @Mock
    private Station testStation;

    // ========== Tests for updateStationStatus ==========
    /**
     * Test OperatorService.updateStationStatus to change station status to ACTIVE.
     * Should set the status from OUT_OF_SERVICE to ACTIVE and save the station.
     */
    @Test
    void updateStationStatusTest_ToActive() {
        StationId stationId = new StationId(1L);
        Station testStation = spy(new Station(stationId, null, null, StationStatus.OUT_OF_SERVICE,
                null, null, 0, 0, List.of(new Dock(null, null, DockStatus.EMPTY))));

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(testStation));

        operatorService.updateStationStatus(stationId, StationStatus.ACTIVE);

        assertThat(testStation.getStationStatus()).isEqualTo(StationStatus.ACTIVE);

        verify(testStation).activateStation();
        verify(stationRepository).save(testStation);
    }

    /**
     * Test OperatorService.updateStationStatus to change station status to OUT_OF_SERVICE.
     * Should set the status from ACTIVE to OUT_OF_SERVICE and save the station.
     */
    @Test
    void updateStationStatusTest_ToOutOfService() {
        StationId stationId = new StationId(1L);
        Station testStation = spy(new Station(stationId, null, null, StationStatus.ACTIVE,
                null, null, 0, 0, List.of(new Dock(null, null, DockStatus.EMPTY))));

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(testStation));

        operatorService.updateStationStatus(stationId, StationStatus.OUT_OF_SERVICE);

        assertThat(testStation.getStationStatus()).isEqualTo(StationStatus.OUT_OF_SERVICE);

        verify(testStation).deactivateStation();
        verify(stationRepository).save(testStation);
    }

    // ========== Tests for rebalanceBike ==========

    /**
     * Test OperatorService.rebalanceBike for a successful bike rebalance operation.
     * Should update bike, source dock, target dock, source station, and target station accordingly.
     */
    @Test
    void rebalanceBikeTest_Successful() {
        OperatorRebalanceDTO dto = mock(OperatorRebalanceDTO.class);

        BikeId bikeId = new BikeId(1L);
        DockId sourceDockId = new DockId(2L);
        DockId targetDockId = new DockId(3L);
        StationId sourceStationId = new StationId(4L);
        StationId targetStationId = new StationId(5L);

        Bike testBike = initializeTestBike(bikeId, sourceDockId, null);
        Dock sourceDock = initializeTestDock(sourceDockId, sourceStationId, DockStatus.OCCUPIED);
        Dock targetDock = initializeTestDock(targetDockId, targetStationId, DockStatus.EMPTY);
        Station sourceStation = intitializeTestStation(sourceStationId, StationAvailability.OCCUPIED,
                StationStatus.ACTIVE, sourceDock);
        Station targetStation = intitializeTestStation(targetStationId, StationAvailability.EMPTY,
                StationStatus.ACTIVE, targetDock);

        when(dto.getBikeId()).thenReturn(1L);
        when(dto.getSourceDockId()).thenReturn(2L);
        when(dto.getTargetDockId()).thenReturn(3L);
        when(dto.getSourceStationId()).thenReturn(4L);
        when(dto.getTargetStationId()).thenReturn(5L);

        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));
        when(dockRepository.findById(sourceDockId)).thenReturn(Optional.of(sourceDock));
        when(dockRepository.findById(targetDockId)).thenReturn(Optional.of(targetDock));
        when(stationRepository.findById(sourceStationId)).thenReturn(Optional.of(sourceStation));
        when(stationRepository.findById(targetStationId)).thenReturn(Optional.of(targetStation));

        operatorService.rebalanceBike(dto);

        assertThat(testBike.getDockId()).isEqualTo(targetDockId);
        assertThat(sourceDock.getStatus()).isEqualTo(DockStatus.EMPTY);
        assertThat(targetDock.getStatus()).isEqualTo(DockStatus.OCCUPIED);
        assertThat(sourceStation.getNumberOfBikesDocked()).isEqualTo(0);
        assertThat(targetStation.getNumberOfBikesDocked()).isEqualTo(1);

        verify(bikeRepository).save(testBike);
        verify(dockRepository).save(sourceDock);
        verify(dockRepository).save(targetDock);
        verify(stationRepository).save(sourceStation);
        verify(stationRepository).save(targetStation);
    }
    // ========== Tests for setBikeForMaintenance ==========

    /**
     * Test OperatorService.setBikeForMaintenance for a successful operation.
     * Should update bike status to MAINTENANCE and dock status to EMPTY.
     */
    @Test
    void setBikeForMaintenanceTest_Successful() {
        BikeId bikeId = new BikeId(1L);
        DockId dockId = new DockId(2L);
        StationId stationId = new StationId(3L);

        Bike testBike = initializeTestBike(bikeId, dockId, BikeStatus.AVAILABLE);
        Dock testDock = initializeTestDock(dockId, stationId, DockStatus.OCCUPIED);

        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));
        when(dockRepository.findById(dockId)).thenReturn(Optional.of(testDock));

        operatorService.setBikeForMaintenance(bikeId, dockId, stationId);

        assertThat(testBike.getStatus()).isEqualTo(BikeStatus.MAINTENANCE);
        assertThat(testBike.getDockId()).isNull();
        assertThat(testDock.getStatus()).isEqualTo(DockStatus.EMPTY);

        verify(bikeRepository).findById(bikeId);
        verify(dockRepository).findById(dockId);
        verify(bikeRepository).save(testBike);
        verify(dockRepository).save(testDock);
    }

    // ========== Tests for removeBikeFromMaintenance ==========
    /**
     * Test OperatorService.removeBikeFromMaintenance for a successful operation.
     * Should update bike status to AVAILABLE and dock status to OCCUPIED.
     */
    @Test
    void removeBikeFromMaintenanceTest_Sucessful() {
        BikeId bikeId = new BikeId(1L);
        DockId dockId = new DockId(2L);
        StationId stationId = new StationId(3L);

        Bike testBike = initializeTestBike(bikeId, dockId, BikeStatus.MAINTENANCE);
        Dock testDock = initializeTestDock(dockId, stationId, DockStatus.EMPTY);

        when(bikeRepository.findById(bikeId)).thenReturn(Optional.of(testBike));
        when(dockRepository.findById(dockId)).thenReturn(Optional.of(testDock));

        operatorService.removeBikeFromMaintenance(bikeId, dockId, stationId);

        assertThat(testBike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
        assertThat(testBike.getDockId()).isEqualTo(dockId);
        assertThat(testDock.getStatus()).isEqualTo(DockStatus.OCCUPIED);

        verify(bikeRepository).findById(bikeId);
        verify(dockRepository).findById(dockId);
        verify(bikeRepository).save(testBike);
        verify(dockRepository).save(testDock);
    }

    private Bike initializeTestBike(BikeId bikeId, DockId dockId, BikeStatus status) {
        return new Bike(bikeId, dockId, status, BikeType.STANDARD, null);
    }

    private Dock initializeTestDock(DockId dockId, StationId stationId, DockStatus status) {
        return new Dock(dockId, stationId, status);
    }

    private Station intitializeTestStation(StationId stationId, StationAvailability availability,
                                           StationStatus status, Dock dock) {
        return new Station(stationId, null, availability,
                status, null, null, 3, 0,
                List.of(dock,
                        new Dock(null, stationId, DockStatus.EMPTY),
                        new Dock(null, stationId, DockStatus.EMPTY)));
    }
}
