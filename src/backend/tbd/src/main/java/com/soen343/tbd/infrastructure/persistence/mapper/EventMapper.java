package com.soen343.tbd.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.infrastructure.persistence.entity.EventEntity;
import com.soen343.tbd.domain.model.ids.EventId;

@Mapper(componentModel = "spring", imports = { EventId.class })
public interface EventMapper {
    // Entity to Domain
    @Mapping(target = "eventId", expression = "java(new EventId(e.getEventId()))")
    Event toDomain(EventEntity e);

    // Domain to Entity
    @Mapping(target = "eventId", expression = "java(e.getEventId() != null ? e.getEventId().value() : null)")
    EventEntity toEntity(Event e);
}
