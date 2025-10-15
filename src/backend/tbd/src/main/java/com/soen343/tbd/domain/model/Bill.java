package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class Bill {
    private BillId billId;
    private Double cost;
    private TripId tripId;
    private UserId userId;

    public Bill(BillId billId, Double cost, TripId tripId, UserId userId){
        this.billId = billId;
        this.cost = cost;
        this.tripId = tripId;
        this.userId = userId;
    }

    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */
    public BillId getBillId() { return billId; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public TripId getTripId() { return tripId; }
    public void setTripId(TripId tripId) { this.tripId = tripId; }
    public UserId getUserId() { return userId; }
    public void setUserId(UserId userId) { this.userId = userId; }

}
