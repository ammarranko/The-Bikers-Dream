package com.soen343.tbd.application.observer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.soen343.tbd.application.dto.MaintenanceUpdateDTO;
import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.domain.model.Bill;

// Concrete Subject 
@Service
public class StationPublisher implements StationSubject {
    private static final Logger logger = LoggerFactory.getLogger(StationPublisher.class);
    private final List<StationObserver> observers = new ArrayList<>();
    private final SSEStationObserver sseStationObserver;

    public StationPublisher(SSEStationObserver sseStationObserver) {
        this.sseStationObserver = sseStationObserver;
    }

    @Override
    public void attach(StationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(StationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(StationDetailsDTO station) {
        logger.debug("Notifying {} observers about station update", observers.size());
        // Notify all observers
        for (StationObserver observer : observers) {
            try {
                observer.update(station);
            } catch (Exception e) {
                logger.error("Error notifying observer: " + e.getMessage());
            }
        }
    }

    @Override
    public void notifyMaintenanceChange(MaintenanceUpdateDTO maintenanceUpdate) {
        logger.debug("Notifying {} observers about maintenance change for bike {}",
                observers.size(), maintenanceUpdate.getBikeId());
        for (StationObserver observer : observers) {
            try {
                observer.onMaintenanceUpdate(maintenanceUpdate);
            } catch (Exception e) {
                logger.error("Error notifying observer about maintenance update: {}", e.getMessage());
            }
        }
    }

    @Override
    public void notifyOperatorEvent(EventDTO event) {
        logger.debug("Notifying about operator event: {}", event.getMetadata());
        sseStationObserver.sendOperatorEvent(event);
    }

}