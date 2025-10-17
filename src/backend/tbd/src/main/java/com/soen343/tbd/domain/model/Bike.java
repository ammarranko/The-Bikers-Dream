package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.BikeType;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;

import java.sql.Timestamp;
import java.util.List;

public class Bike {
    private final BikeId bikeId;
    private DockId dockId;
    private BikeStatus status;
    private BikeType bikeType;
    private Timestamp reservationExpiry;
    List<Trip> trips;

    public Bike(BikeId bikeId, DockId dockId, BikeStatus status, BikeType bikeType, Timestamp reservationExpiry, List<Trip> trips ) {
        this.bikeId = bikeId;
        this.dockId = dockId;
        this.status = status;
        this.bikeType = bikeType;
        this.reservationExpiry = reservationExpiry;
        this.trips = trips;
    }

    public BikeId getBikeId() {
        return bikeId;
    }

    public DockId getDockId() {
        return dockId;
    }

    public void setDockId(DockId dockId) {
        this.dockId = dockId;
    }

    public Timestamp getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Timestamp reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

    public BikeStatus getStatus() {
        return status;
    }

    public void setStatus(BikeStatus status) {
        this.status = status;
    }

    public BikeType getBikeType() {
        return bikeType;
    }

    public void setBikeType(BikeType bikeType) {
        this.bikeType = bikeType;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}
