package com.soen343.tbd.domain.model.helpers;

import java.sql.Timestamp;

import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.ids.EventId;

public class Event {
    private EventId eventId;
    private EntityType entityType;
    private Long entityId;
    private EntityStatus previousState;
    private EntityStatus newState;
    private Timestamp occuredAt;
    private String triggeredBy;
    private String metadata;

    public Event(Long entityId, EntityType entityType, String metadata, EntityStatus newState, Timestamp occuredAt, EntityStatus previousState, String triggeredBy) {
        this.eventId = null; // Set by db automatically
        this.entityId = entityId;
        this.entityType = entityType;
        this.metadata = metadata;
        this.newState = newState;
        this.occuredAt = occuredAt;
        this.previousState = previousState;
        this.triggeredBy = triggeredBy;
    }

    // For mapper
    public Event() {}

    // ------- GETTERS AND SETTERS ---------
    public EventId getEventId() {
        return eventId;
    }

    public void setEventId(EventId eventId) {
        this.eventId = eventId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public EntityStatus getPreviousState() {
        return previousState;
    }

    public void setPreviousState(EntityStatus previousState) {
        this.previousState = previousState;
    }

    public EntityStatus getNewState() {
        return newState;
    }

    public void setNewState(EntityStatus newState) {
        this.newState = newState;
    }

    public Timestamp getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(Timestamp occuredAt) {
        this.occuredAt = occuredAt;
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
}
