package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.enums.StationStatus;
import com.soen343.tbd.domain.model.ids.StationId;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

public class StationTest {

    /**
     * Test incrementBikesDocked method to ensure it updates the number of bikes docked
     * and the station availability correctly.
     */
    @Test
    void incrementBikesDockedTest_UpdatesAvailabilityAndCount() {
        Dock dock1 = new Dock(null, null, DockStatus.EMPTY);
        Dock dock2 = new Dock(null, null, DockStatus.EMPTY);

        Station station = new Station(new StationId(1L), "Test",
                null, StationStatus.ACTIVE, null, null, 2,
                0, Arrays.asList(dock1, dock2));

        station.incrementBikesDocked();

        assertThat(station.getNumberOfBikesDocked()).isEqualTo(1);
        assertThat(station.getStationAvailability()).isEqualTo(StationAvailability.OCCUPIED);

        station.incrementBikesDocked();

        assertThat(station.getNumberOfBikesDocked()).isEqualTo(2);
        assertThat(station.getStationAvailability()).isEqualTo(StationAvailability.FULL);
    }

    /**
     * Test decrementBikesDocked method to ensure it updates the number of bikes docked
     * and the station availability correctly.
     */
    @Test
    void decrementBikesDockedTest_UpdatesAvailabilityAndCount() {
        Dock dock1 = new Dock(null, null, DockStatus.OCCUPIED);
        Dock dock2 = new Dock(null, null, DockStatus.OCCUPIED);

        Station station = new Station(new StationId(1L), "Test", StationAvailability.OCCUPIED,
                StationStatus.ACTIVE, null, null, 2, 2,
                Arrays.asList(dock1, dock2));

        station.decrementBikesDocked();

        assertThat(station.getNumberOfBikesDocked()).isEqualTo(1);
        assertThat(station.getStationAvailability()).isEqualTo(StationAvailability.OCCUPIED);

        station.decrementBikesDocked();

        assertThat(station.getNumberOfBikesDocked()).isEqualTo(0);
        assertThat(station.getStationAvailability()).isEqualTo(StationAvailability.EMPTY);
    }

    @Test
    void calculateNumberOfBikesTest_ReturnsCorrectCount() {
        Dock dock1 = new Dock(null, null, DockStatus.OCCUPIED);
        Dock dock2 = new Dock(null, null, DockStatus.EMPTY);
        Dock dock3 = new Dock(null, null, DockStatus.OCCUPIED);
        List<Dock> docks = Arrays.asList(dock1, dock2, dock3);
        Station station = new Station(new StationId(1L), "Test",
                null, StationStatus.ACTIVE, null, null, 3,
                0, docks);

        assertThat(station.getNumberOfBikesDocked()).isEqualTo(2);
    }
}

