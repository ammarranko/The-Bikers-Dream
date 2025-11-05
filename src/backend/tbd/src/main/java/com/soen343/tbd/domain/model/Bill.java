package com.soen343.tbd.domain.model;

import java.sql.Timestamp;

import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;

public class Bill {
    private BillId billId;
    private Double cost;
    private TripId tripId;
    private UserId userId;

    // Constructor computes cost automatically based on Trip duration and pricing strategy
    public Bill(Trip trip) {
        this.billId = null; // Automatically set by db
        this.tripId = trip.getTripId();
        this.userId = trip.getUserId();
        this.cost = calculateCost(trip);
    }

    // Default constructor for mapper
    public Bill() {}

    private Double calculateCost(Trip trip) {
        if (trip.getStartTime() == null || trip.getEndTime() == null) {
            return 0.0; // Trip hasn't ended yet
        }

        return trip.getPricingStrategy().calculateCost(trip.calculateDurationInMinutes());
    }

    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */
    public BillId getBillId() { return billId; }
    public void setBillId(BillId billId) { this.billId = billId; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public TripId getTripId() { return tripId; }
    public void setTripId(TripId tripId) { this.tripId = tripId; }
    public UserId getUserId() { return userId; }
    public void setUserId(UserId userId) { this.userId = userId; }
}



