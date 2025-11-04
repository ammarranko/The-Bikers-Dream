package com.soen343.tbd.infrastructure.persistence.repository;

import com.soen343.tbd.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.soen343.tbd.domain.model.enums.ReservationStatus;


import java.util.Optional;

@Repository
public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // Find a reservation by bikeId
    Optional<ReservationEntity> findByBike_BikeId(Long bikeId);

    // Find an active reservation by userId using ReservationStatus enum
    Optional<ReservationEntity> findByUser_UserIdAndStatus(Long userId, ReservationStatus status);
}
