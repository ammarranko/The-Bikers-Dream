package com.soen343.tbd.infrastructure.persistence.entity;

import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.BikeType;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "bikes")
public class BikeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bike_id")
    private Long bikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dock_id", referencedColumnName = "dock_id", unique = true)
    private DockEntity dock;

    @Enumerated(EnumType.STRING)
    @Column(name = "bike_status", nullable = false)
    private BikeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "bike_type", nullable = false)
    private BikeType bikeType;

    @Column(name = "reservation_expiry")
    private Timestamp reservationExpiry;

    
    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public DockEntity getDock() {
        return dock;
    }

    public void setDock(DockEntity dock) {
        this.dock = dock;
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

    public Timestamp getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Timestamp reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

}
