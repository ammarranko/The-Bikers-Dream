package com.soen343.tbd.application.dto;

public class RemoveMaintenanceDTO {
    private Long bikeId;
    private Long dockId;
    private Long stationId;

    public RemoveMaintenanceDTO(Long bikeId, Long dockId, Long stationId) {
        this.bikeId = bikeId;
        this.dockId = dockId;
        this.stationId = stationId;
    }

    public Long getBikeId() {
        return bikeId;
    }

    public Long getDockId() {
        return dockId;
    }

    public Long getStationId() {
        return stationId;
    }

}
