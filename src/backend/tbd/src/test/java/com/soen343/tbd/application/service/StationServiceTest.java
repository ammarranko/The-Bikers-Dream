package com.soen343.tbd.application.service;

import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.StationRepository;
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
public class StationServiceTest {
    @Mock
    private StationRepository stationRepository;
    @Mock
    private BikeRepository bikeRepository;
    @InjectMocks
    private StationService stationService;
    @Mock
    private Station testStation;
    @Mock
    private Dock testDock;
    @Mock
    private Bike testBike;

    // ========== Tests for getStationWithDetails ==========

    /**
     * * Test StationService.getStationWithDetails when the station exists.
     * Should return StationDetailsDTO with correct details.
     */
    @Test
    void getStationWithDetailsTest_ReturnsStationDetailsDTO() {
        StationId stationId = new StationId(1L);
        DockId dockId = new DockId(2L);


        when(stationRepository.findById(stationId)).thenReturn(Optional.of(testStation));
        when(bikeRepository.findByDockId(dockId)).thenReturn(Optional.of(testBike));

        when(testStation.getDocks()).thenReturn(List.of(testDock));
        when(testStation.getStationName()).thenReturn("Test Station");
        when(testStation.getStationId()).thenReturn(stationId);
        when(testStation.getStationStatus()).thenReturn(null);
        when(testStation.getStationAvailability()).thenReturn(null);
        when(testStation.getPosition()).thenReturn(null);
        when(testStation.getAddress()).thenReturn(null);
        when(testStation.getCapacity()).thenReturn(1);
        when(testStation.getNumberOfBikesDocked()).thenReturn(1);

        when(testDock.getDockId()).thenReturn(dockId);

        Optional<StationDetailsDTO> result = stationService.getStationWithDetails(stationId.value());

        assertThat(result).isPresent();
        assertThat(result.get().getStationId()).isEqualTo(stationId.value());
        assertThat(result.get().getStationName()).isEqualTo("Test Station");
        assertThat(result.get().getDocks()).hasSize(1);
    }

    /**
     * * Test StationService.getStationWithDetails when the station does not exist.
     * Should return empty Optional.
     */
    @Test
    void getStationWithDetails_StationNotFound_ReturnsEmpty() {
        StationId stationId = new StationId(1L);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        Optional<StationDetailsDTO> result = stationService.getStationWithDetails(stationId.value());

        assertThat(result).isEmpty();
    }

    // ========== Tests for getAllStationsWithDetails ==========
    /**
     * * Test StationService.getAllStationsWithDetails.
     * Should return list of StationDetailsDTO with correct details.
     */
    @Test
    void getAllStationsWithDetailsTest_ReturnsListOfStationDetailsDTO() {
        DockId dockId = new DockId(1L);
        when(testDock.getDockId()).thenReturn(dockId);

        when(stationRepository.findAll()).thenReturn(List.of(testStation));
        when(testStation.getDocks()).thenReturn(List.of(testDock));
        when(bikeRepository.findByDockId(dockId)).thenReturn(Optional.of(testBike));

        when(testStation.getStationName()).thenReturn("Test Station");
        when(testStation.getStationId()).thenReturn(new StationId(1L));
        when(testStation.getStationStatus()).thenReturn(null);
        when(testStation.getStationAvailability()).thenReturn(null);
        when(testStation.getPosition()).thenReturn(null);
        when(testStation.getAddress()).thenReturn(null);
        when(testStation.getCapacity()).thenReturn(1);
        when(testStation.getNumberOfBikesDocked()).thenReturn(1);

        List<StationDetailsDTO> result = stationService.getAllStationsWithDetails();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStationName()).isEqualTo("Test Station");
        assertThat(result.get(0).getDocks()).hasSize(1);
    }
}
