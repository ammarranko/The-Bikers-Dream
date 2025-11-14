package com.soen343.tbd.application.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.soen343.tbd.application.dto.DockUpdateContextDTO;
import com.soen343.tbd.application.dto.MaintenanceUpdateDTO;
import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.application.dto.StationDetailsDTO;
import com.soen343.tbd.application.dto.StationDetailsDTO.DockWithBikeDTO;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.application.dto.StationDetailsDTO.BikeDTO;

@Component
public class SSEStationObserver implements StationObserver {
    private static final Logger logger = LoggerFactory.getLogger(SSEStationObserver.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            logger.debug("SSE connection completed");
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            logger.debug("SSE connection timed out");
            emitters.remove(emitter);
        });
        emitter.onError((e) -> {
            logger.error("SSE connection error: " + e.getMessage());
            emitters.remove(emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE Connection Established"));
            logger.debug("New SSE connection established. Total connections: {}", emitters.size());
        } catch (Exception e) {
            logger.error("Error sending initial heartbeat: " + e.getMessage());
            emitters.remove(emitter);
            throw new RuntimeException("Failed to establish SSE connection", e);
        }

        return emitter;
    }

    @Override
    public void update(StationDetailsDTO station) {
        if (emitters.isEmpty()) {
            logger.debug("No active SSE connections to notify");
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        List<DockWithBikeDTO> docks = station.getDocks();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("station-update")
                        .data(station));
                logger.debug("Sent station update to SSE client");

                for (DockWithBikeDTO dock : docks) {
                    BikeDTO bike = dock.getBike();

                    DockUpdateContextDTO dockUpdate = new DockUpdateContextDTO(
                            station.getStationId(),
                            station.getStationName(),
                            dock);

                    emitter.send(SseEmitter.event()
                            .name("dock-update")
                            .data(dockUpdate));
                    logger.debug("Sent dock update for station {}, dock {}",
                            station.getStationId(), dock.getDockId());
                }
            } catch (IOException e) {
                logger.error("Error sending update to SSE client: " + e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            logger.debug("Removed {} dead SSE connections. Remaining connections: {}",
                    deadEmitters.size(), emitters.size());
        }
    }

    @Override
    public void onMaintenanceUpdate(MaintenanceUpdateDTO maintenanceUpdateDTO) {
        if (emitters.isEmpty()) {
            logger.debug("No active SSE connections to notify for maintenance update");
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("maintenance-update")
                        .data(maintenanceUpdateDTO));
                logger.debug("Sent maintenance update to SSE client for bike ID: {}",
                        maintenanceUpdateDTO.getBikeId());
            } catch (IOException e) {
                logger.error("Error sending maintenance update to SSE client: " + e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            logger.debug("Removed {} dead SSE connections after maintenance update. Remaining connections: {}",
                    deadEmitters.size(), emitters.size());
        }
    }

    public void sendOperatorBillUpdate(Map<String, Object> billData) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("operator-bill-update")
                        .data(billData));
                logger.debug("Sent operator bill update for bill ID: {}", billData.get("billId"));
            } catch (IOException ioe) {
                logger.error("Error sending operator bill update to SSE client: " + ioe.getMessage());
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            logger.debug("Removed {} dead SSE connections after operator bill update.", deadEmitters.size());
        }
    }

    // special method only for operators
    @Override
    public void sendOperatorEvent(EventDTO event) {
        if (emitters.isEmpty()) {
            logger.debug("No active Operator SSE connections to notify.");
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("operator-event").data(event));
            } catch (IOException ioe) {
                logger.error("Error sending operator event to SSE client: " + ioe.getMessage());
                deadEmitters.add(emitter);
            }
        });

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
            logger.debug("Removed {} dead SSE connections. Remaining connections: {}",
                    deadEmitters.size(), emitters.size());
        }
    }

}