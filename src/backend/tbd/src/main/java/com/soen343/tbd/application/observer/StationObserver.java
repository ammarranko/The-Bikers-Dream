package com.soen343.tbd.application.observer;

import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.application.dto.MaintenanceUpdateDTO;
import com.soen343.tbd.application.dto.StationDetailsDTO;

// Define contracts for Subscriber (Observer) in Observer pattern
public interface StationObserver {
    public void update(StationDetailsDTO station); // New data available

    default void onMaintenanceUpdate(MaintenanceUpdateDTO maintenanceUpdate) {
        // Optional method for maintenance updates
    }

    default void sendOperatorEvent(EventDTO event) {
        // Optional method for operator events
    }
}
