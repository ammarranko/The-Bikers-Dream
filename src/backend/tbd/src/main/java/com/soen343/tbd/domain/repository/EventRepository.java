package com.soen343.tbd.domain.repository;

import java.util.Optional;
import java.util.List;

import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.domain.model.ids.EventId;

public interface EventRepository {
    List<Event> findEventsByEntityType(EntityType entityType);

    List<Event> findEventsByEntityId(Long entityId);

    Optional<Event> findById(EventId eventId);

    Event save(Event event);
}
