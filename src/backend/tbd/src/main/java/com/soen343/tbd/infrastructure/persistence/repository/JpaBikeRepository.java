package com.soen343.tbd.infrastructure.persistence.repository;

import java.util.List;

import com.soen343.tbd.infrastructure.persistence.entity.BikeEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.soen343.tbd.domain.model.enums.BikeStatus;

@Repository
public interface JpaBikeRepository extends JpaRepository<BikeEntity, Long> {
    Optional<BikeEntity> findByDock_DockId(Long dockId);

    List<BikeEntity> findByStatus(BikeStatus status);
}
