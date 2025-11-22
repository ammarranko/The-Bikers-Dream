package com.soen343.tbd.application.dto;

import java.sql.Timestamp;

public class ReturnResponse {
    // Trip information
    private Long tripId;
    private Long bikeId;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userTier;
    private String startStationName;
    private String endStationName;
    private Timestamp startTime;
    private Timestamp endTime;
    private Double durationMinutes;

    // Bill information
    private Long billId;
    private String pricingStrategy;
    private Double baseFare;
    private Double perMinuteRate;
    private Double regularCost;
    private Double discountedCost;
    private Integer flexMoneyEarned;
    private Double flexMoneyUsed;
    private Double loyaltyDiscount;
    private Integer flexMoneyBalance;

    public ReturnResponse(Long tripId, Long bikeId, Long userId, String userFullName, String userEmail,
                          String startStationName, String endStationName,
                          Timestamp startTime, Timestamp endTime, Double durationMinutes,
                          Long billId, String pricingStrategy, Double baseFare, Double perMinuteRate,
                          String userTier, Double regularCost, Double discountedCost, Integer flexMoneyEarned, Double flexMoneyUsed,
                          Double loyaltyDiscount, Integer flexMoneyBalance) { 
        this.tripId = tripId;
        this.bikeId = bikeId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.billId = billId;
        this.pricingStrategy = pricingStrategy;
        this.baseFare = baseFare;
        this.perMinuteRate = perMinuteRate;
        this.userTier = userTier;
        this.regularCost = regularCost;
        this.discountedCost = discountedCost;
        this.flexMoneyEarned = flexMoneyEarned;
        this.flexMoneyUsed = flexMoneyUsed;
        this.loyaltyDiscount = loyaltyDiscount;
        this.flexMoneyBalance = flexMoneyBalance;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStartStationName() {
        return startStationName;
    }

    public void setStartStationName(String startStationName) {
        this.startStationName = startStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
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

    public Double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(String pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public Double getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(Double baseFare) {
        this.baseFare = baseFare;
    }

    public Double getPerMinuteRate() {
        return perMinuteRate;
    }

    public void setPerMinuteRate(Double perMinuteRate) {
        this.perMinuteRate = perMinuteRate;
    }

    public String getUserTier() {
        return userTier;
    }

    public void setUserTier(String userTier) {
        this.userTier = userTier;
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

    public Integer getFlexMoneyEarned() {
        return flexMoneyEarned;
    }

    public void setFlexMoneyEarned(Integer flexMoneyEarned) {
        this.flexMoneyEarned = flexMoneyEarned;
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

    public Integer getFlexMoneyBalance() {
        return flexMoneyBalance;
    }
}
