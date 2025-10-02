package com.soen343.tbd.domain.model.bike;

import com.soen343.tbd.domain.model.ids.BikeId;

import java.time.Instant;

public class Bike {
    private final BikeId id;
    private BikeStatus status;
    private BikeType type;
    private Instant reservationExpiry;

    public Bike(BikeId id, BikeStatus status, BikeType type, Instant reservationExpiry ) {
        this.id = id;
        this.status = status;
        this.type = type;
        this.reservationExpiry = reservationExpiry;
    }

    public BikeId getId() {
        return id;
    }

    public Instant getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Instant reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

    public BikeStatus getStatus() {
        return status;
    }

    public void setStatus(BikeStatus status) {
        this.status = status;
    }

    public BikeType getType() {
        return type;
    }

    public void setType(BikeType type) {
        this.type = type;
    }
}
