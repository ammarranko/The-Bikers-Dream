package com.soen343.tbd.infrastructure.persistence.entity;
import com.soen343.tbd.domain.model.enums.TripStatus;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "trips") // Schema is now handled by configuration
public class TripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TripStatus status;

    // Direct ID columns to avoid lazy loading
    @Column(name = "bike_id", insertable = false, updatable = false)
    private Long bikeId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "start_station_id", insertable = false, updatable = false)
    private Long startStationId;

    @Column(name = "end_station_id", insertable = false, updatable = false)
    private Long endStationId;

    // Relationship entities (lazy loaded only when explicitly accessed)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id")
    private BikeEntity bike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_station_id")
    private StationEntity startStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_station_id")
    private StationEntity endStation;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "pricing_strategy")
    private String pricingStrategy;

    @OneToOne(mappedBy="trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillEntity bill;

    /*

      GETTERS AND SETTERS
    -----------------------
    */

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    // Direct ID getters (no lazy loading)
    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(Long startStationId) {
        this.startStationId = startStationId;
    }

    public Long getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(Long endStationId) {
        this.endStationId = endStationId;
    }

    // Relationship entity getters (may trigger lazy loading)
    public BikeEntity getBike() {
        return bike;
    }

    public void setBike(BikeEntity bike) {
        this.bike = bike;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public StationEntity getStartStation() {
        return startStation;
    }

    public void setStartStation(StationEntity startStation) {
        this.startStation = startStation;
    }

    public StationEntity getEndStation() {
        return endStation;
    }

    public void setEndStation(StationEntity endStation) {
        this.endStation = endStation;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(String pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public BillEntity getBill() {
        return bill;
    }

    public void setBill(BillEntity bill) {
        this.bill = bill;
    }

}