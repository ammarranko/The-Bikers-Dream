package com.soen343.tbd.application.dto;

import com.soen343.tbd.domain.model.enums.BikeType;

public class TripDetailsDTO {
    private Long tripId;
    private Long bikeId;
    private BikeType bikeType;
    private Long userId;
    private Long startStationId;
    private Long endStationId;
    private String startTime;
    private String endTime;
    private String status;
    private Long billId;

    public TripDetailsDTO() {
    }

    public TripDetailsDTO(Long tripId, Long bikeId, Long userId, Long startStationId,
                          Long endStationId, String startTime, String endTime,
                          String status, Long billId, BikeType bikeType) {
        this.tripId = tripId;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.billId = billId;
        this.bikeType = bikeType;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public void setBikeType(BikeType bikeType) {
        this.bikeType = bikeType;
    }

    public BikeType getBikeType() {
        return bikeType;
    }
}

