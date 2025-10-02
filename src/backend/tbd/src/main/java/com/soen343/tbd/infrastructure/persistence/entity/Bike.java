package com.soen343.tbd.infrastructure.persistence.entity;

import com.soen343.tbd.domain.model.bike.BikeStatus;
import com.soen343.tbd.domain.model.bike.BikeType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bikes")
public class Bike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bike_id")
    private Long bikeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BikeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "bike_type", nullable = false)
    private BikeType bikeType;

    @Column(name = "reservation_expiry")
    private Instant reservationExpiry;

    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
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

    public Instant getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Instant reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

}
