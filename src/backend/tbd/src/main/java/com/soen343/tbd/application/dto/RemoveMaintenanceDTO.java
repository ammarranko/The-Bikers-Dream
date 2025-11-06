package com.soen343.tbd.application.dto;

public class RemoveMaintenanceDTO {
    private Long bikeId;
    private Long dockId;

    public RemoveMaintenanceDTO(Long bikeId, Long dockId) {
        this.bikeId = bikeId;
        this.dockId = dockId;
    }

    public Long getBikeId() {
        return bikeId;
    }

    public Long getDockId() {
        return dockId;
    }
}
