package com.soen343.tbd.application.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.soen343.tbd.application.dto.StationDetailsDTO;

@Service
public class StationPublisher implements StationSubject {
    private final List<StationObserver> observers = new ArrayList<>();
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // SSE subscription for riders (frontend)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
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
        // Notify all observers
        for (StationObserver observer : observers) {
            observer.update(station);
        }

        // send SSE updates to all connected frontend clients
        sendSSEUpdate(station);
    }

    private void sendSSEUpdate(StationDetailsDTO station) {
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