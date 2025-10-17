package com.soen343.tbd.infrastructure.persistence.entity;

import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.enums.StationStatus;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "stations")
public class StationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "station_availability", nullable = false)
    private StationAvailability stationAvailability;

    @Enumerated(EnumType.STRING)
    @Column(name = "station_status", nullable = false)
    private StationStatus stationStatus;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
    private List<DockEntity> docks;

    @OneToMany(mappedBy = "startStation", fetch = FetchType.LAZY)
    private List<TripEntity> startedTrips;

    @OneToMany(mappedBy = "endStation", fetch = FetchType.LAZY)
    private List<TripEntity> endedTrips;

    
    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public StationAvailability getStationAvailability() {
        return stationAvailability;
    }

    public void setStationAvailability(StationAvailability stationAvailability) {
        this.stationAvailability = stationAvailability;
    }

    public StationStatus getStationStatus() {
        return stationStatus;
    }

    public void setStationStatus(StationStatus stationStatus) {
        this.stationStatus = stationStatus;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<DockEntity> getDocks() {
        return docks;
    }

    public void setDocks(List<DockEntity> docks) {
        this.docks = docks;
    }

    public List<TripEntity> getStartedTrips() {
        return startedTrips;
    }

    public void setStartedTrips(List<TripEntity> startedTrips) {
        this.startedTrips = startedTrips;
    }

    public List<TripEntity> getEndedTrips() {
        return endedTrips;
    }

    public void setEndedTrips(List<TripEntity> endedTrips) {
        this.endedTrips = endedTrips;
    }

}
