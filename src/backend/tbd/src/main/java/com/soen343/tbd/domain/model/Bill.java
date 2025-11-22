package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.BillStatus;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class Bill {
    private BillId billId;
    private Double regularCost; // Not persisted - calculated for display only
    private Double discountedCost; // This is the actual cost paid, persisted as 'cost' in DB
    private Double flexMoneyUsed; // Not persisted - calculated for display only
    private Integer flexMoneyEarned; // Not persisted - calculated for display only
    private Double loyaltyDiscount; // Not persisted - calculated for display only
    private TripId tripId;
    private UserId userId;
    private BillStatus status;

    // Constructor computes cost automatically based on Trip duration and pricing strategy
    public Bill(Trip trip, double discountRate) {
        this.billId = null; // Automatically set by db
        this.tripId = trip.getTripId();
        this.userId = trip.getUserId();
        this.regularCost = calculateCost(trip, 0.0);
        this.discountedCost = calculateCost(trip, discountRate);
        this.status = BillStatus.PENDING;
    }

    // Default constructor for mapper
    public Bill() {}

    private Double calculateCost(Trip trip, double discountRate) {
        if (trip.getStartTime() == null || trip.getEndTime() == null) {
            return 0.0; // Trip hasn't ended yet
        }

        Double baseCost = trip.getPricingStrategy().calculateCost(trip.calculateDurationInMinutes());

        // Apply discount
        Double discount = baseCost * discountRate;
        return baseCost - discount;
    }

    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public BillId getBillId() {
        return billId;
    }

    public void setBillId(BillId billId) {
        this.billId = billId;
    }

    public Double getRegularCost() {
        return regularCost;
    }

    public void setRegularCost(Double regularCost) {
        this.regularCost = regularCost;
    }

    public Double getDiscountedCost() {
        return discountedCost;
    }

    public void setDiscountedCost(Double discountedCost) {
        this.discountedCost = discountedCost;
    }

    public TripId getTripId() {
        return tripId;
    }

    public void setTripId(TripId tripId) {
        this.tripId = tripId;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public BillStatus getStatus() {
        return status;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public Double getFlexMoneyUsed() {
        return flexMoneyUsed;
    }

    public void setFlexMoneyUsed(Double flexMoneyUsed) {
        this.flexMoneyUsed = flexMoneyUsed;
    }

    public Double getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    public void setLoyaltyDiscount(Double loyaltyDiscount) {
        this.loyaltyDiscount = loyaltyDiscount;
    }

    public Integer getFlexMoneyEarned() {
        return flexMoneyEarned;
    }

    public void setFlexMoneyEarned(Integer flexMoneyEarned) {
        this.flexMoneyEarned = flexMoneyEarned;
    }
}



