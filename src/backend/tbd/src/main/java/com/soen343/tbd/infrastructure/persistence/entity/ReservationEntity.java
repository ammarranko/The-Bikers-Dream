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

    // Direct ID columns to avoid lazy loading
    @Column(name = "bike_id", insertable = false, updatable = false)
    private Long bikeId;

    @Column(name = "start_station_id", insertable = false, updatable = false)
    private Long startStationId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    // Relationship entities (lazy loaded only when explicitly accessed)
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

    // Optional: future link to a Bill or Payment entity
    // @OneToOne(mappedBy="reservation", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    // private BillEntity bill;

    public ReservationEntity() {}

    // Getters and Setters
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    // Direct ID getters (no lazy loading)
    public Long getBikeId() { return bikeId; }
    public void setBikeId(Long bikeId) { this.bikeId = bikeId; }

    public Long getStartStationId() { return startStationId; }
    public void setStartStationId(Long startStationId) { this.startStationId = startStationId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    // Relationship entity getters (may trigger lazy loading)
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
