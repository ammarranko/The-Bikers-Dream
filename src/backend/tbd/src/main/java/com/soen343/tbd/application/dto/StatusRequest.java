package com.soen343.tbd.application.dto;

// for operator to change station status

public class StatusRequest {
    Long stationId;
    String status;

    public Long getStationId() {
        return stationId;
    }
    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
