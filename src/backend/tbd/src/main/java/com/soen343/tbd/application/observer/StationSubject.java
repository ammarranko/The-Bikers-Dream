package com.soen343.tbd.application.observer;

import com.soen343.tbd.application.dto.MaintenanceUpdateDTO;

// Observer package in application layer because it's part of app's business logic

import com.soen343.tbd.application.dto.StationDetailsDTO;

// Define contracts for Publisher (Subject) in Observer pattern
// Interface - SSEStationObserver implements it
public interface StationSubject {
    void attach(StationObserver observer); // Subscribe to updates

    void detach(StationObserver observer); // Unsubscribe from updates

    void notifyObservers(StationDetailsDTO station); // Broadcast updates to all subscribers

    default void notifyMaintenanceChange(MaintenanceUpdateDTO maintenanceUpdate){}; // Broadcast maintenance updates optionally
}
