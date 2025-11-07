package com.soen343.tbd.application.dto;

import java.sql.Timestamp;
import com.soen343.tbd.domain.model.helpers.Event;

public class EventDTO {
    
    private Long eventId;
    private String entityType;
    private Long entityId;
    private String triggeredBy;
    private String metadata;
    private String previousState;
    private String newState;
    private Timestamp occuredAt;

    public EventDTO() {
        this.occuredAt = new Timestamp(System.currentTimeMillis());
    }

    public EventDTO(Long entityId, String entityType, Long eventId, String metadata, 
                    String newState, String previousState, String triggeredBy, Timestamp occuredAt) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.eventId = eventId;
        this.metadata = metadata;
        this.newState = newState;
        this.occuredAt = occuredAt != null ? occuredAt : new Timestamp(System.currentTimeMillis());
        this.previousState = previousState;
        this.triggeredBy = triggeredBy;
    }

    /**
     * Convert Event domain model to EventDTO with null safety
     * @param event The event to convert
     * @return EventDTO representation of the event
     * @throws IllegalArgumentException if event is null or missing required fields
     */
    public static EventDTO fromEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot create EventDTO from null event");
        }

        // Safely extract previous state
        String previousState = event.getPreviousState() != null 
            ? event.getPreviousState().name() 
            : null;

        // Safely extract event ID (may be null if not persisted yet)
        Long eventId = event.getEventId() != null 
            ? event.getEventId().value() 
            : null;

        // Validate required fields
        if (event.getEntityType() == null) {
            throw new IllegalArgumentException("Event must have an entity type");
        }
        if (event.getNewState() == null) {
            throw new IllegalArgumentException("Event must have a new state");
        }

        return new EventDTO(
            event.getEntityId(),
            event.getEntityType().name(),
            eventId,
            event.getMetadata(),
            event.getNewState().name(),
            previousState,
            event.getTriggeredBy(),
            event.getOccuredAt()
        );
    }

    // Getters and setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public Timestamp getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(Timestamp occuredAt) {
        this.occuredAt = occuredAt;
    }
}