package com.soen343.tbd.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.enums.StationStatus;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for displaying a complete station view with all docks and bikes
 */
@JsonPropertyOrder({
    "stationId",
    "stationName",
    "stationAvailability",
    "stationStatus",
    "position",
    "address",
    "capacity",
    "numberOfBikesDocked",
    "docks"
})
public class StationDetailsDTO {
    private Long stationId;
    private String stationName;
    private StationAvailability stationAvailability;
    private StationStatus stationStatus;
    private String position;
    private String address;
    private int capacity;
    private int numberOfBikesDocked;
    private final List<DockWithBikeDTO> docks;

    public StationDetailsDTO(Station station, List<Dock> docks, List<Bike> bikes) {
        this.stationId = station.getStationId().value();
        this.stationName = station.getStationName();
        this.stationAvailability = station.getStationAvailability();
        this.stationStatus = station.getStationStatus();
        this.position = station.getPosition();
        this.address = station.getAddress();
        this.capacity = station.getCapacity();
        this.numberOfBikesDocked = station.getNumberOfBikesDocked();

        // Map docks with their bikes
        this.docks = docks.stream()
                .map(dock -> {
                    // Find bike for this dock
                    Bike bike = bikes.stream()
                            .filter(b -> b.getDockId() != null && b.getDockId().equals(dock.getDockId()))
                            .findFirst()
                            .orElse(null);
                    return new DockWithBikeDTO(dock, bike);
                })
                .collect(Collectors.toList());
    }

    // Getters
    public Long getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public StationAvailability getStationAvailability() {
        return stationAvailability;
    }

    public StationStatus getStationStatus() {
        return stationStatus;
    }

    public String getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumberOfBikesDocked() {
        return numberOfBikesDocked;
    }

    public List<DockWithBikeDTO> getDocks() {
        return docks;
    }

    /**
     * Nested DTO for dock with its bike (if any)
     */
    public static class DockWithBikeDTO {
        private Long dockId;
        private DockStatus dockStatus;
        private BikeDTO bike;

        public DockWithBikeDTO(Dock dock, Bike bike) {
            this.dockId = dock.getDockId().value();
            this.dockStatus = dock.getStatus();
            this.bike = bike != null ? new BikeDTO(bike) : null;
        }

        public Long getDockId() {
            return dockId;
        }

        public DockStatus getDockStatus() {
            return dockStatus;
        }

        public BikeDTO getBike() {
            return bike;
        }
    }

    /**
     * Nested DTO for bike information
     */
    public static class BikeDTO {
        private Long bikeId;
        private BikeStatus status;
        private BikeType bikeType;
        private Timestamp reservationExpiry;

        public BikeDTO(Bike bike) {
            this.bikeId = bike.getBikeId().value();
            this.status = bike.getStatus();
            this.bikeType = bike.getBikeType();
            this.reservationExpiry = bike.getReservationExpiry();
        }

        public Long getBikeId() {
            return bikeId;
        }

        public BikeStatus getStatus() {
            return status;
        }

        public BikeType getBikeType() {
            return bikeType;
        }

        public Timestamp getReservationExpiry() {
            return reservationExpiry;
        }
    }
}
