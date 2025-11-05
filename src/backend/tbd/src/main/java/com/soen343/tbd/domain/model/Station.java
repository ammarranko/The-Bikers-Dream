package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.enums.StationAvailability;
import com.soen343.tbd.domain.model.enums.StationStatus;
import com.soen343.tbd.domain.model.ids.StationId;

import java.util.List;

import com.soen343.tbd.domain.model.enums.DockStatus;


// IMPORTANT TODO:
// NEED TO IMPLEMENT STATION AVAILABILITY LOGIC
public class Station {
    private final StationId stationId;
    private final String stationName;
    private StationAvailability stationAvailability;
    private StationStatus stationStatus;
    private final String position;
    private final String address;
    private final int capacity;     // Total number of docks at the station
    private int numberOfBikesDocked;
    private final List<Dock> docks;

    public Station(StationId stationId, String stationName, StationAvailability stationAvailability,
                   StationStatus stationStatus, String position, String address, int capacity,
                   int numberOfBikesDocked, List<Dock> docks) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.stationAvailability = stationAvailability;
        this.stationStatus = stationStatus;
        this.position = position;
        this.address = address;
        this.capacity = capacity;
        this.docks = docks;

        this.numberOfBikesDocked = calculateNumberOfBikes();
    }

    private int calculateNumberOfBikes(){
        int numBikes = 0;

        for (Dock dock : docks){
            if(dock.getStatus().equals(DockStatus.OCCUPIED)){
                numBikes++;
            }
        }

        return numBikes;
    }

    public StationId getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
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

    /* for operator to change station status 
     uses the setter
     also uses the enum so it doesn't take invalid input by accident
    */
    public void activateStation() {
        setStationStatus(StationStatus.ACTIVE);
    }

    public void deactivateStation() {
        setStationStatus(StationStatus.OUT_OF_SERVICE);
    }

    public String getPosition() {
        return position;
    }

    public String getAddress() {
        return address;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumberOfBikesDocked() {
        return numberOfBikesDocked;
    }

    public void incrementBikesDocked(){
        this.numberOfBikesDocked++;
        if (this.numberOfBikesDocked == this.capacity) {
            this.stationAvailability = StationAvailability.FULL;
        } else if (this.numberOfBikesDocked > 0) {
            this.stationAvailability = StationAvailability.OCCUPIED;
        }
    }

    public void decrementBikesDocked(){
        this.numberOfBikesDocked--;
        if (this.numberOfBikesDocked < this.capacity) {
            this.stationAvailability = StationAvailability.EMPTY;
        } else if (this.numberOfBikesDocked > 0) {
            this.stationAvailability = StationAvailability.OCCUPIED;
        }
    }

    public List<Dock> getDocks() {
        return docks;
    }
}
