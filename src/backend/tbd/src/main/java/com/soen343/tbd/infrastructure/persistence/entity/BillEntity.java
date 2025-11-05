package com.soen343.tbd.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bills")
public class BillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "cost")
    private Double cost;

    // Direct ID columns to avoid lazy loading
    @Column(name = "trip_id", insertable = false, updatable = false)
    private Long tripId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    // Relationship entities (lazy loaded only when explicitly accessed)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /*
      GETTERS AND SETTERS
    -----------------------
    */

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }


    // Direct ID getters (no lazy loading)
    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Relationship entity getters (may trigger lazy loading)
    public TripEntity getTrip() {
        return trip;
    }

    public void setTrip(TripEntity trip) {
        this.trip = trip;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
