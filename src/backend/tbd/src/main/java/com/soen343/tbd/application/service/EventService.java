package com.soen343.tbd.application.service;

import java.sql.Timestamp;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.domain.repository.EventRepository;
import com.soen343.tbd.application.dto.EventDTO;
import com.soen343.tbd.application.observer.StationSubject;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StationSubject stationPublisher;

    /**
     * Creates and persists an event for a specific entity
     * @return The persisted event with generated ID, or null if creation fails
     */
    @Transactional
    public Event createEventForEntity(EntityType entityType, Long entityId, String metadataString, 
                                     EntityStatus previousState, EntityStatus newState, String triggeredBy) {
        logger.debug("Creating event for entityType: {}, entityId: {}, previousState: {}, newState: {}", 
            entityType, entityId, previousState, newState);
        
        try {
            // Validate inputs
            if (entityType == null || entityId == null || newState == null) {
                logger.error("Cannot create event with null required fields. EntityType: {}, EntityId: {}, NewState: {}", 
                    entityType, entityId, newState);
                return null;
            }

            // Create event
            Timestamp occuredAt = Timestamp.from(Instant.now());
            Event event = new Event(entityId, entityType, metadataString, newState, occuredAt, previousState, triggeredBy);
            
            // Persist and return with generated ID
            Event savedEvent = eventRepository.save(event);
            
            logger.info("Event created successfully - ID: {}, EntityType: {}, EntityId: {}, NewState: {}", 
                savedEvent.getEventId() != null ? savedEvent.getEventId().value() : "null", 
                entityType, entityId, newState);
            
            return savedEvent;
            
        } catch (Exception e) {
            logger.error("Failed to create event for entityType: {}, entityId: {} - Error: {}", 
                entityType, entityId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Notifies all operators about an event
     * @param eventDTO The event to broadcast
     */
    public void notifyAllOperatorsWithEvent(EventDTO eventDTO) {
        if (eventDTO == null) {
            logger.warn("Cannot notify operators with null eventDTO");
            return;
        }

        try {
            stationPublisher.notifyOperatorEvent(eventDTO);
            logger.debug("Notified all operators about event: {} for entity: {} #{}", 
                eventDTO.getEventId(), eventDTO.getEntityType(), eventDTO.getEntityId());
        } catch (Exception e) {
            logger.error("Failed to notify operators for event: {} - Error: {}", 
                eventDTO.getEventId(), e.getMessage(), e);
        }
    }

    /**
     * Helper method to safely create and notify about an event
     * Returns true if successful, false otherwise
     */
    public boolean createAndNotifyEvent(EntityType entityType, Long entityId, String metadata,
                                       EntityStatus previousState, EntityStatus newState, String triggeredBy) {
        try {
            Event event = createEventForEntity(entityType, entityId, metadata, previousState, newState, triggeredBy);
            if (event != null) {
                EventDTO eventDTO = EventDTO.fromEvent(event);
                notifyAllOperatorsWithEvent(eventDTO);
                return true;
            }
        } catch (Exception e) {
            logger.error("Failed to create and notify event for entityType: {}, entityId: {}", 
                entityType, entityId, e);
        }
        return false;
    }
}
