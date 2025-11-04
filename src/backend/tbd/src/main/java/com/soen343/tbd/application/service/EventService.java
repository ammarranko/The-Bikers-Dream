package com.soen343.tbd.application.service;

import java.sql.Timestamp;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.domain.repository.EventRepository;

@Service
public class EventService {
    Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    public void createEventForEntity(EntityType entityType, Long entityId, String metadataString, EntityStatus previousState, EntityStatus newState, String triggeredBy) {
        // DEBUG
        logger.debug("Creating event for entityType: {}, entityId: {}", entityType, entityId);
        
        try {
            Timestamp occuredAt = Timestamp.from(Instant.now());
            Event event = new Event(entityId, entityType, metadataString, newState, occuredAt, previousState, triggeredBy);
            eventRepository.save(event);

            logger.info("Event created for entityType: {}, entityId: {}, newState: {}", entityType, entityId, newState);
        } catch (Exception e) {
            // Handle exception (e.g., log the error)
            logger.warn("Event creation failed: {}", e.getMessage());
        }
        
    }
}
