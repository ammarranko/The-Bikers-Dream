package com.soen343.tbd.infrastructure.persistence.repository;

import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface JpaTripRepository extends JpaRepository<TripEntity, Long> {
    Optional<TripEntity> findByUser_UserIdAndStatus(Long userId, TripStatus status);

    List<TripEntity> findAllByUser_UserId(Long userId);

    Optional<TripEntity> findByTripIdAndUser_Email(Long tripId, String email);

    List<TripEntity> findByUser_Email(String email);

    @Override
    Optional<TripEntity> findById(Long aLong);

    // Count trips for a user since a specific date
    @Query("SELECT COUNT(t) FROM TripEntity t WHERE t.user.userId = :userId AND t.startTime >= :since")
    int countTripsForUserByIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Count unreturned bikes (ongoing trips) for a user
    @Query("SELECT COUNT(t) FROM TripEntity t WHERE t.user.userId = :userId AND t.status = :status")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TripStatus status);

    // Count trips for a user between two dates
    @Query("SELECT COUNT(t) FROM TripEntity t WHERE t.user.userId = :userId AND t.startTime >= :start AND t.startTime < :end")
    int countTripsForUserBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}