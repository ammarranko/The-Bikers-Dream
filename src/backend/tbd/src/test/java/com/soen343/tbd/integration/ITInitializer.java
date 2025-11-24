package com.soen343.tbd.integration;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.*;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;

import java.util.List;

public class ITInitializer {
    public static User initializeRider() {
        User rider = new Rider(null, "John Doe", "IT@email.com",
                "password", "123 Main St", "johndoe",
                new java.sql.Timestamp(System.currentTimeMillis()), null, 0);
        return rider;
    }

    public static Bike initializeBike(BikeId bikeId, DockId dockId) {
        Bike bike = new Bike(bikeId, dockId, BikeStatus.AVAILABLE, BikeType.STANDARD, null);
        return bike;
    }

    public static Dock initializeDock(DockId dockId, StationId stationId) {
        Dock dock = new Dock(dockId, stationId, DockStatus.EMPTY);
        return dock;
    }

    public static Station initializeStation(StationId stationId, String name, List<Dock> docks) {
        Station station = new Station(stationId, name, StationAvailability.EMPTY,
                StationStatus.ACTIVE,"test position " + name, "123 Main St " + name,
                docks.size(), 0, docks);
        return station;
    }
}
