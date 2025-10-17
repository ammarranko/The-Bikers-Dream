package com.soen343.tbd.domain.model;

import java.sql.Timestamp;

import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class Bill {
    private BillId billId;
    private Double cost;
    private TripId tripId;
    private UserId userId;

    // cost per minute
    private static final double COST_PER_MINUTE = 0.5;
    
    // Constructor computes cost automatically based on Trip duration
    public Bill(Trip trip) {
        this.billId = null; // Automatically set by db
        this.tripId = trip.getTripId();
        this.userId = trip.getUserId();
        this.cost = calculateCost(trip.getStartTime(), trip.getEndTime());
    }

    // Default Constructor since there is ambiguity with constructors for BillMapper
    public Bill() {

    }

    // Method to calculate cost from start and end timestamps
    private Double calculateCost(Timestamp startTime, Timestamp endTime) {
        if (startTime == null || endTime == null) {
            return 0.0; // Trip hasn't ended yet
        }
        // Duration in minutes
        long durationMillis = endTime.getTime() - startTime.getTime();
        double durationMinutes = durationMillis / 60000.0;
        return durationMinutes * COST_PER_MINUTE;
    }

    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */
    public BillId getBillId() { return billId; }
    public void setBillId(BillId billId) { this.billId = billId; } // Need this due to needing a write accessor and cant have custom constructor
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public TripId getTripId() { return tripId; }
    public void setTripId(TripId tripId) { this.tripId = tripId; }
    public UserId getUserId() { return userId; }
    public void setUserId(UserId userId) { this.userId = userId; }

}
