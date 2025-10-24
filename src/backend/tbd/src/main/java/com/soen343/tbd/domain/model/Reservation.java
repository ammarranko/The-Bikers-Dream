package com.soen343.tbd.domain.model;

import java.sql.Timestamp;
import com.soen343.tbd.domain.model.enums.ReservationStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.ids.ReservationId;

public class Reservation {

    private ReservationId reservationId;
    private BikeId bikeId;
    private StationId startStationId;
    private UserId userId;
    private Timestamp reservedAt;
    private Timestamp expiresAt;
    private ReservationStatus status;

    // Constructor for new active reservation
    public Reservation(ReservationId reservationId, BikeId bikeId, StationId startStationId, UserId userId, Timestamp reservedAt, Timestamp expiresAt) {
        this.reservationId = reservationId;
        this.bikeId = bikeId;
        this.startStationId = startStationId;
        this.userId = userId;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
        this.status = ReservationStatus.ACTIVE;
    }

    // Default constructor (for mapper)
    public Reservation() {}

    // -----------------------
    //       GETTERS
    // -----------------------
    public ReservationId getReservationId() { return reservationId; }
    public BikeId getBikeId() { return bikeId; }
    public StationId getStartStationId() { return startStationId; }
    public UserId getUserId() { return userId; }
    public Timestamp getReservedAt() { return reservedAt; }
    public Timestamp getExpiresAt() { return expiresAt; }
    public ReservationStatus getStatus() { return status; }

    // -----------------------
    //       SETTERS
    // -----------------------
    public void setReservationId(ReservationId reservationId) { this.reservationId = reservationId; }
    public void setBikeId(BikeId bikeId) { this.bikeId = bikeId; }
    public void setStartStationId(StationId startStationId) { this.startStationId = startStationId; }
    public void setUserId(UserId userId) { this.userId = userId; }
    public void setReservedAt(Timestamp reservedAt) { this.reservedAt = reservedAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    // -----------------------
    //      BUSINESS LOGIC
    // -----------------------
    public void cancel() {
        if (status != ReservationStatus.ACTIVE) throw new IllegalStateException("Reservation cannot be cancelled because it is not active.");
        status = ReservationStatus.CANCELLED;
    }

    public void complete() {
        if (status != ReservationStatus.ACTIVE) throw new IllegalStateException("Reservation cannot be completed because it is not active.");
        status = ReservationStatus.COMPLETED;
    }

    public boolean isExpired() {
        return expiresAt.before(new Timestamp(System.currentTimeMillis()));
    }
}
