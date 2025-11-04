package com.soen343.tbd.infrastructure.persistence.repository;

import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaStationRepository extends JpaRepository<StationEntity, Long> {
}

