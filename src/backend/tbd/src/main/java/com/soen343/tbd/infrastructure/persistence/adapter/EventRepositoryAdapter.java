package com.soen343.tbd.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.domain.model.helpers.Event;
import com.soen343.tbd.domain.model.ids.EventId;
import com.soen343.tbd.domain.repository.EventRepository;
import com.soen343.tbd.infrastructure.persistence.mapper.EventMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaEventRepository;

@Repository
public class EventRepositoryAdapter  implements EventRepository{
    @Autowired
    JpaEventRepository jpaEventRepository;

    @Autowired
    EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Event> findEventsByEntityType(EntityType entityType) {
        return jpaEventRepository.findAllByEntityType(entityType)
                .stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findEventsByEntityId(Long entityId) {
        return jpaEventRepository.findAllByEntityId(entityId)
                .stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        return jpaEventRepository.findById(eventId.value())
                .map(eventMapper::toDomain);
    }

    @Override
    public void save(Event event) {
        var eventEntity = eventMapper.toEntity(event);
        jpaEventRepository.save(eventEntity);
    }
}
