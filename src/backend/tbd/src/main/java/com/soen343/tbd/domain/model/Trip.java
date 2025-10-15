package com.soen343.tbd.domain.model;

import java.sql.Timestamp;

import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class Trip {
    private final TripId tripId;
    private TripStatus status;
    private BikeId bikeId;
    private UserId userId;
    private StationId startStationId;
    private StationId endStationId;
    private Timestamp startTime;
    private Timestamp endTime;
    private BillId billId;

    // Constructor for when starting a trip
    public Trip(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId){
        this.tripId = tripId;
        this.status = TripStatus.ONGOING;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationId = startStationId;
        this.endStationId = null;
        this.startTime = new Timestamp(System.currentTimeMillis());
        this.endTime = null;
        this.billId = null;
    }

    // Constructor if you want to create a completed trip
    public Trip(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId, StationId endStationId, Timestamp startTime, Timestamp endTime, BillId billId) {
        this.tripId = tripId;
        this.status = TripStatus.COMPLETED;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.billId = billId;
    }

    /**
     * Ends the trip, sets end time and station, creates a Bill automatically,
     * and stores its BillId in this trip.
     */
    public Bill endTrip(StationId endStationId) {
        if (this.status == TripStatus.COMPLETED) {
            throw new IllegalStateException("Trip is already completed.");
        }

        this.endStationId = endStationId;
        this.endTime = new Timestamp(System.currentTimeMillis());
        this.status = TripStatus.COMPLETED;

        // Create the Bill automatically
        Bill bill = new Bill(this); // Bill constructor computes cost automatically
        this.billId = bill.getBillId(); // store the BillId in the Trip

        return bill; // return the Bill so the caller can persist it
    }

    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */
    public TripId getTripId() { return tripId; }
    public TripStatus getStatus() { return status; }
    public BikeId getBikeId() { return bikeId; }
    public UserId getUserId() { return userId; }
    public StationId getStartStationId() { return startStationId; }
    public StationId getEndStationId() { return endStationId; }
    public Timestamp getStartTime() { return startTime; }
    public Timestamp getEndTime() { return endTime; }
    public BillId getBillId() { return billId; }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public void setBikeId(BikeId bikeId) {
        this.bikeId = bikeId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public void setStartStationId(StationId startStationId) {
        this.startStationId = startStationId;
    }

    public void setEndStationId(StationId endStationId) {
        this.endStationId = endStationId;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setBillId(BillId billId) {
        this.billId = billId;
    }
}
