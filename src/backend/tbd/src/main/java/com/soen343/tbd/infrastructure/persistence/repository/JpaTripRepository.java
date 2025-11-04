package com.soen343.tbd.infrastructure.persistence.repository;

import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JpaTripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByUser_UserIdAndStatus(Long userId, TripStatus status);
}