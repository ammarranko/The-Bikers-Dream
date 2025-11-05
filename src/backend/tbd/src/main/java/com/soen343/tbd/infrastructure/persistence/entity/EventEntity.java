package com.soen343.tbd.infrastructure.persistence.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.soen343.tbd.domain.model.enums.EntityStatus;
import com.soen343.tbd.domain.model.enums.EntityType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "state_transitions")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_state", nullable = false)
    private EntityStatus previousState;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_state", nullable = false)
    private EntityStatus newState;

    @Column(name = "timestamp", nullable = false)
    private Timestamp occuredAt;

    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String metadata;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
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
