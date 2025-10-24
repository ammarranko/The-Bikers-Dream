package com.soen343.tbd.application.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.soen343.tbd.application.dto.StationDetailsDTO;

// SSE = Server-Sent Events
// Link between Observer pattern and SSE implementation - Allows real-time
// updates

@Component
public class SSEStationObserver implements StationObserver {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>(); //
    // Thread-safe list,won'tthrow// ConcurrentModificationException because
    // creates copy of underlying array when
    // adding/removing emitters.

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void update(StationDetailsDTO station) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("station-update")
                        .data(station));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }
}