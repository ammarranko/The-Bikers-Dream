package com.soen343.tbd.application.observer;

import com.soen343.tbd.application.dto.StationDetailsDTO;

// Define contracts for Subscriber (Observer) in Observer pattern
public interface StationObserver {
    public void update(StationDetailsDTO station); // New data available

}
