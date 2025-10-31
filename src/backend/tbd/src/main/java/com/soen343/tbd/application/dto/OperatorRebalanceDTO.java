package com.soen343.tbd.application.dto;

// DTO for rebalancing bikes

public class OperatorRebalanceDTO {

    private Long bikeId;
    private Long sourceDockId;
    private Long targetDockId;
    private Long sourceStationId;
    private Long targetStationId;

    // Getters and Setters
    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public Long getSourceDockId() {
        return sourceDockId;
    }

    public void setSourceDockId(Long sourceDockId) {
        this.sourceDockId = sourceDockId;
    }

    public Long getTargetDockId() {
        return targetDockId;
    }

    public void setTargetDockId(Long targetDockId) {
        this.targetDockId = targetDockId;
    }

    public Long getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(Long sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public Long getTargetStationId() {
        return targetStationId;
    }

    public void setTargetStationId(Long targetStationId) {
        this.targetStationId = targetStationId;
    }
}

/* reasons
 using long instead of objectsID since frontend does not know objects 
 esp when link passing
 */