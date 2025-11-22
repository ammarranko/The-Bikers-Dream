package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.enums.DockStatus;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemResetService {

    private static final Logger logger = LoggerFactory.getLogger(SystemResetService.class);

    private final BillRepository billRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final TripRepository tripRepository;
    private final BikeRepository bikeRepository;
    private final DockRepository dockRepository;

    public SystemResetService(BillRepository billRepository,
                              ReservationRepository reservationRepository,
                              EventRepository eventRepository,
                              TripRepository tripRepository,
                              BikeRepository bikeRepository,
                              DockRepository dockRepository) {
        this.billRepository = billRepository;
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.tripRepository = tripRepository;
        this.bikeRepository = bikeRepository;
        this.dockRepository = dockRepository;
    }

    @Transactional
    public void resetSystem() {
        logger.info("Starting system reset...");
        
        // 1. Delete all transactional data
        // Using deleteAllInBatch for performance and to avoid potential N+1 or cascading issues during delete
        logger.info("Deleting all bills...");
        billRepository.deleteAllInBatch();
        
        logger.info("Deleting all reservations...");
        reservationRepository.deleteAllInBatch();
        
        logger.info("Deleting all events...");
        eventRepository.deleteAllInBatch();
        
        logger.info("Deleting all trips...");
        tripRepository.deleteAllInBatch();

        // 2. Rebalance stations
        logger.info("Rebalancing stations...");
        rebalanceStations();
        logger.info("System reset completed successfully.");
    }

    private void rebalanceStations() {
        logger.info("Fetching all bikes and docks...");
        List<Bike> allBikes = bikeRepository.findAll();
        List<Dock> allDocks = dockRepository.findAll();
        logger.info("Found {} bikes and {} docks.", allBikes.size(), allDocks.size());

        // Reset all docks to EMPTY
        for (Dock dock : allDocks) {
            dock.setStatus(DockStatus.EMPTY);
        }
        // We save docks later, or we can save now. 
        // Saving now to ensure state is clean before assigning bikes.
        dockRepository.saveAll(allDocks);

        // Reset all bikes to MAINTENANCE (temp) and clear current dock/reservation
        // We use MAINTENANCE because the DB constraint requires dock_id IS NULL -> status IN ('ON_TRIP', 'MAINTENANCE')
        for (Bike bike : allBikes) {
            bike.setStatus(BikeStatus.MAINTENANCE);
            bike.setReservationExpiry(null);
            bike.setDockId(null); // Clear dock association to avoid unique constraint violations
        }
        // Save cleared state first
        bikeRepository.saveAll(allBikes);
        bikeRepository.flush(); // Force flush to DB to ensure docks are free

        // Group docks by station
        Map<StationId, List<Dock>> docksByStation = allDocks.stream()
                .collect(Collectors.groupingBy(Dock::getStationId));

        List<StationId> stationIds = new ArrayList<>(docksByStation.keySet());
        if (stationIds.isEmpty()) {
            logger.warn("No stations found to rebalance.");
            return;
        }

        // Distribute bikes round-robin across stations
        int bikeIndex = 0;
        int stationIndex = 0;
        int bikesAssigned = 0;
        int totalBikes = allBikes.size();

        // While we have bikes to assign
        while (bikesAssigned < totalBikes) {
            StationId stationId = stationIds.get(stationIndex);
            List<Dock> stationDocks = docksByStation.get(stationId);

            // Find an empty dock in this station
            Dock emptyDock = stationDocks.stream()
                    .filter(d -> d.getStatus() == DockStatus.EMPTY)
                    .findFirst()
                    .orElse(null);

            if (emptyDock != null) {
                Bike bike = allBikes.get(bikeIndex);
                
                // Assign bike to dock
                bike.setDockId(emptyDock.getDockId());
                bike.setStatus(BikeStatus.AVAILABLE); // Set back to AVAILABLE now that it has a dock
                emptyDock.setStatus(DockStatus.OCCUPIED);
                
                bikeIndex++;
                bikesAssigned++;
            }

            // Move to next station
            stationIndex = (stationIndex + 1) % stationIds.size();
            
            // Safety break if all docks are full
             if (bikesAssigned < totalBikes && allDocks.stream().allMatch(d -> d.getStatus() == DockStatus.OCCUPIED)) {
                 logger.warn("All docks are full. Remaining {} bikes could not be docked.", totalBikes - bikesAssigned);
                 break;
             }
        }
        
        // Save changes
        logger.info("Saving updated docks and bikes...");
        dockRepository.saveAll(allDocks);
        bikeRepository.saveAll(allBikes);
        logger.info("Rebalancing completed.");
    }
}
