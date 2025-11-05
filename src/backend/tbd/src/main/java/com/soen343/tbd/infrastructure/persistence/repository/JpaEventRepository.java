package com.soen343.tbd.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soen343.tbd.domain.model.enums.EntityType;
import com.soen343.tbd.infrastructure.persistence.entity.EventEntity;

@Repository
public interface JpaEventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findAllByEntityId(Long entityId);
    List<EventEntity> findAllByEntityType(EntityType entityType);
}    