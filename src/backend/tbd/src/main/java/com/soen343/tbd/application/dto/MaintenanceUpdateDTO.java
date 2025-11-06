package com.soen343.tbd.application.dto;

// DTO for maintenance updates
public class MaintenanceUpdateDTO {
    private Long bikeId;
    private String bikeStatus;
    private Long stationId;
    private String stationName;
    private Long dockId;
    private String action; // "ADDED" or "REMOVED"

    public MaintenanceUpdateDTO(Long bikeId, String bikeStatus, Long stationId, 
                                String stationName, Long dockId, String action) {
        this.bikeId = bikeId;
        this.bikeStatus = bikeStatus;
        this.stationId = stationId;
        this.stationName = stationName;
        this.dockId = dockId;
        this.action = action;
    }

    public Long getBikeId() { return bikeId; }
    public void setBikeId(Long bikeId) { this.bikeId = bikeId; }
    
    public String getBikeStatus() { return bikeStatus; }
    public void setBikeStatus(String bikeStatus) { this.bikeStatus = bikeStatus; }
    
    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    
    public Long getDockId() { return dockId; }
    public void setDockId(Long dockId) { this.dockId = dockId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
