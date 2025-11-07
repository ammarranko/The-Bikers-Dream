package com.soen343.tbd.application.dto;

import com.soen343.tbd.domain.model.Bike;

public class FetchBikeMaintenanceResponse {
    private Long bikeId;
    private String status;
    private Long dockId;
    private String bikeType;

    public FetchBikeMaintenanceResponse(Bike bike) {
        this.bikeId = bike.getBikeId().value();
        this.status = bike.getStatus().name();
        this.dockId = bike.getDockId() != null ? bike.getDockId().value() : null;
        this.bikeType = bike.getBikeType().name();
    }

    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDockId() {
        return dockId;
    }

    public void setDockId(Long dockId) {
        this.dockId = dockId;
    }

    public String getBikeType() {
        return bikeType;
    }

    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }

    
}
