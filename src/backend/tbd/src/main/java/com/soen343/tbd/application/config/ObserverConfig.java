package com.soen343.tbd.application.config;

import org.springframework.context.annotation.Configuration;

import com.soen343.tbd.application.observer.SSEStationObserver;
import com.soen343.tbd.application.observer.StationPublisher;

import jakarta.annotation.PostConstruct;

@Configuration
public class ObserverConfig {
    private final StationPublisher stationPublisher;
    private final SSEStationObserver sseObserver;

    // public ObserverConfig(StationSubject publisher, SSEStationObserver observer)
    // {
    // this.publisher = publisher;
    // this.observer = observer;
    // }

    // This implementation of registering observer to publisher could
    // cause attachment to happen too late (after notify)
    // @PostConstruct
    // public void setupObservers() {
    // publisher.attach(observer);
    // }

    public ObserverConfig(StationPublisher stationPublisher, SSEStationObserver sseObserver) {
        this.stationPublisher = stationPublisher;
        this.sseObserver = sseObserver;
    }

    @PostConstruct
    public void setupObservers() {
        // Register SSEStationObserver with the publisher
        stationPublisher.attach(sseObserver);
    }

    // @Bean
    // public StationPublisher stationPublisher(SSEStationObserver
    // sseStationObserver) {
    // return new StationPublisher(); // Publisher created first because not set as
    // class attribute
    // }

    // @Bean
    // public SSEStationObserver sseStationObserver(StationPublisher
    // stationPublisherImpl) {
    // SSEStationObserver observer = new SSEStationObserver();
    // stationPublisherImpl.attach(observer);
    // return observer;
    // }
}
