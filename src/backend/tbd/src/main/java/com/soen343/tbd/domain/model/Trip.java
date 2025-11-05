package com.soen343.tbd.domain.model;

import java.sql.Timestamp;

import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;


public class Trip {
    private TripId tripId;
    private TripStatus status;
    private BikeId bikeId;
    private UserId userId;
    private BillId billId;
    private StationId startStationId;
    private StationId endStationId;
    private Timestamp startTime;
    private Timestamp endTime;
    private PricingStrategy pricingStrategy;


    // Constructor for when starting a trip
    public Trip(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId,
                PricingStrategy pricingStrategy) {
        this.tripId = tripId;
        this.status = TripStatus.ONGOING;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationId = startStationId;
        this.endStationId = null;
        this.startTime = new Timestamp(System.currentTimeMillis());
        this.endTime = null;
        this.billId = null;
        this.pricingStrategy = pricingStrategy;
    }

    // Constructor if you want to create a completed trip
    public Trip(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId,
                StationId endStationId, Timestamp startTime, Timestamp endTime, BillId billId) {
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

    // Default Constructor for mapper to clear ambiguity
    public Trip(){}

    /**
     * Ends the trip, sets end time and station, creates a Bill automatically using the trip's pricing strategy,
     * and stores its BillId in this trip.
     */
    public Bill endTrip(StationId endStationId) {
        if (this.status == TripStatus.COMPLETED) {
            throw new IllegalStateException("Trip is already completed.");
        }

        this.endStationId = endStationId;
        this.endTime = new Timestamp(System.currentTimeMillis());
        this.status = TripStatus.COMPLETED;

        // Create bill using the trip's pricing strategy
        Bill bill = new Bill(this);
        this.billId = bill.getBillId();

        return bill;
    }

    /**
     * Calculates the duration of the trip in minutes.
     * Returns 0 if the trip hasn't ended yet.
     * Returns fractional minutes (e.g., 30 seconds = 0.5 minutes).
     */
    public double calculateDurationInMinutes() {
        if (this.startTime == null || this.endTime == null) {
            return 0.0;
        }
        long durationMillis = this.endTime.getTime() - this.startTime.getTime();
        return durationMillis / 60000.0; // Convert milliseconds to minutes (with decimals)
    }

/*
-----------------------
  GETTERS AND SETTERS
-----------------------
*/

    public TripId getTripId() {
        return tripId;
    }

    public void setTripId(TripId tripId) {
        this.tripId = tripId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public BikeId getBikeId() {
        return bikeId;
    }

    public void setBikeId(BikeId bikeId) {
        this.bikeId = bikeId;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public BillId getBillId() {
        return billId;
    }

    public void setBillId(BillId billId) {
        this.billId = billId;
    }

    public StationId getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(StationId startStationId) {
        this.startStationId = startStationId;
    }

    public StationId getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(StationId endStationId) {
        this.endStationId = endStationId;
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

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }
}
