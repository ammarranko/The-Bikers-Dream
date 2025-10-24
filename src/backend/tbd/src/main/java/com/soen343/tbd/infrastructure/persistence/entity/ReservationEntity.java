package com.soen343.tbd.infrastructure.persistence.entity;

import com.soen343.tbd.domain.model.enums.ReservationStatus;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id")
    private BikeEntity bike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id")
    private StationEntity startStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "reserved_at")
    private Timestamp reservedAt;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    // --------------------
    // Constructors
    // --------------------
    public ReservationEntity() {}

    public ReservationEntity(Long reservationId, BikeEntity bike, StationEntity startStation, UserEntity user,
                             Timestamp reservedAt, Timestamp expiresAt, ReservationStatus status) {
        this.reservationId = reservationId;
        this.bike = bike;
        this.startStation = startStation;
        this.user = user;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    // --------------------
    // Getters and Setters
    // --------------------
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public BikeEntity getBike() { return bike; }
    public void setBike(BikeEntity bike) { this.bike = bike; }

    public StationEntity getStartStation() { return startStation; }
    public void setStartStation(StationEntity startStation) { this.startStation = startStation; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public Timestamp getReservedAt() { return reservedAt; }
    public void setReservedAt(Timestamp reservedAt) { this.reservedAt = reservedAt; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}

