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
    @Column(name = "status") // if column name has underscore
    private TripStatus status;

    @ManyToOne
    @JoinColumn(name = "bike_id")
    private BikeEntity bike;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "start_station_id")
    private StationEntity startStation;

    @ManyToOne
    @JoinColumn(name = "end_station_id")
    private StationEntity endStation;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @OneToOne(mappedBy="trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillEntity bill;


    /* 
    -----------------------
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

    public BillEntity getBill() {
        return bill;
    }

    public void setBill(BillEntity bill) {
        this.bill = bill;
    }

}