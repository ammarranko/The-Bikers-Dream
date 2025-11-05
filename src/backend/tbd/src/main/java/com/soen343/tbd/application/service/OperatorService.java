package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.enums.StationStatus;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.application.dto.OperatorRebalanceDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* 1 operator able to change station status: active/outOFservice
 2 operator can rebalance a bike (move from one dock to another) */
@Service
public class OperatorService {

    private static final Logger logger = LoggerFactory.getLogger(OperatorService.class);

    private final StationRepository stationRepository;
    private final BikeRepository bikeRepository;
    private final DockRepository dockRepository;
    private final EventService eventService;

    public OperatorService(BikeRepository bikeRepository, DockRepository dockRepository, StationRepository stationRepository, EventService eventService) {
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
        this.stationRepository = stationRepository;
        this.eventService = eventService;
    }

    // allows operator toggle between active/outOFservice for station
    public void updateStationStatus(StationId stationId, StationStatus newStatus) {
        // get station
        Station station = stationRepository.findById(stationId)
            .orElseThrow(() -> new RuntimeException("Station not found, ID: " + stationId.value()));

        // Current status
        StationStatus currentStatus = station.getStationStatus();

        switch (newStatus) {
            case ACTIVE:
                station.activateStation();
                logger.debug("Station: {} active", station.getStationId());
                break;
            case OUT_OF_SERVICE:
                station.deactivateStation();
                logger.debug("Station: {} out of service", station.getStationId());
                break;
            default:
                logger.warn("Not a station status: {}", newStatus);
                throw new IllegalArgumentException("Not a station status: " + newStatus);
        }

        // Create event for station status change
        eventService.createEventForEntity(
                EntityType.STATION,
                station.getStationId().value(),
                "Station status changed",
                EntityStatus.fromSpecificStatus(currentStatus),
                EntityStatus.fromSpecificStatus(newStatus),
                "System"
        );

        // save updated station
        stationRepository.save(station);
        
        logger.info("Station ID: {} new status: {}", station.getStationId(), newStatus);
    }

    // rebalance allows operator to move one bike at a time, doesn't create trips
    @Transactional
    public void rebalanceBike(OperatorRebalanceDTO dto) {
        // get the dto long ids into id objects
        BikeId bikeId = new BikeId(dto.getBikeId());
        DockId sourceDockId = new DockId(dto.getSourceDockId());
        DockId targetDockId = new DockId(dto.getTargetDockId());
        StationId sourceStationId = new StationId(dto.getSourceStationId());
        StationId targetStationId = new StationId(dto.getTargetStationId());

        // get objects from repos using object ids
        Bike selectedBike = bikeRepository.findById(bikeId)
            .orElseThrow(() -> new RuntimeException("Missing bike " + bikeId.value()));
        Dock sourceDock = dockRepository.findById(sourceDockId)
            .orElseThrow(() -> new RuntimeException("Missing source dock " + sourceDockId.value())); 
        Dock targetDock = dockRepository.findById(targetDockId)
            .orElseThrow(() -> new RuntimeException("Missing target dock " + targetDockId.value()));
        Station sourceStation = stationRepository.findById(sourceStationId)
            .orElseThrow(() -> new RuntimeException("Missing source station " + sourceStationId.value()));
        Station targetStation = stationRepository.findById(targetStationId)
            .orElseThrow(() -> new RuntimeException("Missing target station " + targetStationId.value()));
         
        // now move the bike & update everything 

        // move bike
        selectedBike.setDockId(targetDockId);
        bikeRepository.save(selectedBike);
        logger.info("Bike {} - target dock {} - target station {}", 
        bikeId.value(), targetDockId.value(), targetStationId.value());

        // source dock empty
        sourceDock.setStatus(DockStatus.EMPTY);
        dockRepository.save(sourceDock);
        logger.info("Source dock {} empty", sourceDockId.value());

        // Create event for dock status change - source dock
        eventService.createEventForEntity(
                EntityType.DOCK,
                sourceDock.getDockId().value(),
                "Dock status changed",
                EntityStatus.OCCUPIED,
                EntityStatus.EMPTY,
                "System"
        );

        // target dock occupied
        targetDock.setStatus(DockStatus.OCCUPIED);
        dockRepository.save(targetDock);
        logger.info("Target dock {} occupied", targetDockId.value());

        // Create event for dock status change - target dock
        eventService.createEventForEntity(
                EntityType.DOCK,
                targetDock.getDockId().value(),
                "Dock status changed",
                EntityStatus.EMPTY,
                EntityStatus.OCCUPIED,
                "System"
        );

        // decrease source station count

        // Deterimine previous status for event logging
        EntityStatus previousStatus = EntityStatus.fromSpecificStatus(sourceStation.getStationStatus());

        sourceStation.decrementBikesDocked();
        stationRepository.save(sourceStation);
        logger.info("Source station {} bike count--", sourceStationId.value());

        // Determine new status for event logging
        EntityStatus newStatus = EntityStatus.fromSpecificStatus(sourceStation.getStationStatus());

        // Only create event if status has changed
        if (previousStatus != newStatus) {
            // Create event for station bike count change - source station
            eventService.createEventForEntity(
                EntityType.STATION,
                sourceStation.getStationId().value(),
                "Station bike count changed",
                previousStatus,
                newStatus,
                "System");
        }

        // increase target station count

        // Determine previous status for event logging
        previousStatus = EntityStatus.fromSpecificStatus(targetStation.getStationStatus());

        targetStation.incrementBikesDocked();
        stationRepository.save(targetStation);
        logger.info("Target station {} bike count++", targetStationId.value());

        // Determine new status for event logging
        newStatus = EntityStatus.fromSpecificStatus(targetStation.getStationStatus());

        // Only create event if status has changed
        if (previousStatus != newStatus) {
            // Create event for station bike count change - target station
            eventService.createEventForEntity(
                EntityType.STATION,
                targetStation.getStationId().value(),
                "Station bike count changed",
                previousStatus,
                newStatus,
                "System");
        }

    }
}
