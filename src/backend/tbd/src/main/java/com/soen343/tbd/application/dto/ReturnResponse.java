package com.soen343.tbd.application.dto;

import java.sql.Timestamp;

public class ReturnResponse {
    // Trip information
    private Long tripId;
    private Long bikeId;
    private Long userId;
    private String startStationName;
    private String endStationName;
    private Timestamp startTime;
    private Timestamp endTime;
    private Double durationMinutes;

    // Bill information
    private Long billId;
    private Double baseFare;
    private Double perMinuteRate;
    private Double totalAmount;

    public ReturnResponse(Long tripId, Long bikeId, Long userId,
                         String startStationName, String endStationName,
                         Timestamp startTime, Timestamp endTime, Double durationMinutes,
                         Long billId, Double baseFare, Double perMinuteRate,
                         Double totalAmount) {
        this.tripId = tripId;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.billId = billId;
        this.baseFare = baseFare;
        this.perMinuteRate = perMinuteRate;
        this.totalAmount = totalAmount;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

}
