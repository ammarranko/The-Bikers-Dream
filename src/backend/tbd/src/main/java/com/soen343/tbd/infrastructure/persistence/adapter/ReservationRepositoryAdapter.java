package com.soen343.tbd.infrastructure.persistence.adapter;

import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.ids.ReservationId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.infrastructure.persistence.entity.BikeEntity;
import com.soen343.tbd.infrastructure.persistence.entity.UserEntity;
import com.soen343.tbd.infrastructure.persistence.mapper.ReservationMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaReservationRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import com.soen343.tbd.domain.model.enums.ReservationStatus;
import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public class ReservationRepositoryAdapter implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationMapper reservationMapper;
    private final EntityManager entityManager;

    public ReservationRepositoryAdapter(JpaReservationRepository jpaReservationRepository,
                                        ReservationMapper reservationMapper,
                                        EntityManager entityManager) {
        this.jpaReservationRepository = jpaReservationRepository;
        this.reservationMapper = reservationMapper;
        this.entityManager = entityManager;
    }

    @Override
    public List<Reservation> findAll() {
        return jpaReservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAll() {
        jpaReservationRepository.deleteAll();
    }

    @Override
    public void deleteAllInBatch() {
        jpaReservationRepository.deleteAllInBatch();
    }

    @Override
    public Optional<Reservation> findById(ReservationId reservationId) {
        return jpaReservationRepository.findById(reservationId.value())
                .map(reservationMapper::toDomain);
    }

    @Override
    public Optional<Reservation> checkActiveReservationByUserId(UserId userId) {
        return jpaReservationRepository
                .findByUser_UserIdAndStatus(userId.value(), ReservationStatus.ACTIVE)
                .map(reservationMapper::toDomain);
    }

    @Override
    public void save(Reservation reservation) {
        var reservationEntity = reservationMapper.toEntity(reservation);

        if (reservation.getBikeId() != null) {
            BikeEntity bikeReference = entityManager.getReference(BikeEntity.class, reservation.getBikeId().value());
            reservationEntity.setBike(bikeReference);
        }

        if (reservation.getUserId() != null) {
            UserEntity userReference = entityManager.getReference(UserEntity.class, reservation.getUserId().value());
            reservationEntity.setUser(userReference);
        }
        // Set the starting station relationship if startStationId is present
        if (reservation.getStartStationId() != null) {
            StationEntity stationReference = entityManager.getReference(StationEntity.class, reservation.getStartStationId().value());
            reservationEntity.setStartStation(stationReference);
        }


        jpaReservationRepository.save(reservationEntity);
    }

    @Override
    public int missedReservationscount(UserId userId, LocalDateTime oneYearAgo) {
        return jpaReservationRepository.countByUserIdAndStatusSince(userId.value(), ReservationStatus.EXPIRED, oneYearAgo);
    }

}
