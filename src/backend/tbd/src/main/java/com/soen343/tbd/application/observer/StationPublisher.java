package com.soen343.tbd.application.observer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.soen343.tbd.application.dto.StationDetailsDTO;


// Concrete Subject 
@Service
public class StationPublisher implements StationSubject {
    private static final Logger logger = LoggerFactory.getLogger(StationPublisher.class);
    private final List<StationObserver> observers = new ArrayList<>();

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

//    private void sendSSEUpdate(StationDetailsDTO station) {
//        List<SseEmitter> deadEmitters = new ArrayList<>();
//        List<DockWithBikeDTO> docks = station.getDocks();
//
//        emitters.forEach(emitter -> {
//            try {
//                // Send station-level update
//                emitter.send(SseEmitter.event()
//                    .name("station-update")
//                    .data(station));
//
//                // Send individual dock updates with context
//                for (DockWithBikeDTO dock : docks) {
//                    DockUpdateContextDTO dockUpdate = new DockUpdateContextDTO(
//                        station.getStationId(),
//                        station.getStationName(),
//                        dock
//                    );
//                    emitter.send(SseEmitter.event()
//                        .name("dock-update")
//                        .data(dockUpdate));
//                }
//            } catch (Exception e) {
//                deadEmitters.add(emitter);
//                logger.warn("Failed to send update to emitter: " + e.getMessage());
//                try {
//                    emitter.complete();
//                } catch (Exception ex) {
//                    logger.error("Failed to complete dead emitter: " + ex.getMessage());
//                }
//            }
//        });
//
//        if (!deadEmitters.isEmpty()) {
//            emitters.removeAll(deadEmitters);
//            logger.info("Removed {} dead emitters", deadEmitters.size());
//        }
//    }
}