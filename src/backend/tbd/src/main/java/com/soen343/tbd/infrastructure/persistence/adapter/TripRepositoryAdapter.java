package com.soen343.tbd.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.infrastructure.persistence.entity.BikeEntity;
import com.soen343.tbd.infrastructure.persistence.entity.BillEntity;
import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;
import com.soen343.tbd.infrastructure.persistence.entity.UserEntity;
import com.soen343.tbd.infrastructure.persistence.mapper.PricingStrategyConverter;
import com.soen343.tbd.infrastructure.persistence.mapper.TripMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaTripRepository;

import jakarta.persistence.EntityManager;

@Repository
public class TripRepositoryAdapter implements TripRepository {
    private final JpaTripRepository jpaTripRepository;
    private final TripMapper tripMapper;
    private final EntityManager entityManager;

    public TripRepositoryAdapter(JpaTripRepository jpa, TripMapper mapper, EntityManager entityManager) {
        this.jpaTripRepository = jpa;
        this.tripMapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Trip> checkRentalsByUserId(UserId userId) {
        return jpaTripRepository.findByUser_UserIdAndStatus(userId.value(), TripStatus.ONGOING)
                .map(tripMapper::toDomain);
    }

    @Override
    public Optional<Trip> findById(TripId tripId) {
        return jpaTripRepository.findById(tripId.value())
                .map(tripMapper::toDomain);
    }

    @Override
    public Trip save(Trip trip) {
        var tripEntity = tripMapper.toEntity(trip);

        // Set the bike relationship if tripId is present
        if (trip.getBikeId() != null) {
            BikeEntity bikeReference = entityManager.getReference(BikeEntity.class, trip.getBikeId().value());
            tripEntity.setBike(bikeReference);
        }

        // Set the user relationship if userId is present
        if (trip.getUserId() != null) {
            UserEntity userReference = entityManager.getReference(UserEntity.class, trip.getUserId().value());
            tripEntity.setUser(userReference);
        }

        // Set the starting station relationship if startStationId is present
        if (trip.getStartStationId() != null) {
            StationEntity stationReference = entityManager.getReference(StationEntity.class,
                    trip.getStartStationId().value());
            tripEntity.setStartStation(stationReference);
        }

        // Set the ending station relationship if endStationId is present
        if (trip.getEndStationId() != null) {
            StationEntity stationReference = entityManager.getReference(StationEntity.class,
                    trip.getEndStationId().value());
            tripEntity.setEndStation(stationReference);
        }

        // Set the bill relationship if billId is present
        if (trip.getBillId() != null) {
            BillEntity billReference = entityManager.getReference(BillEntity.class, trip.getBillId().value());
            tripEntity.setBill(billReference);
        }

        // Persist additional primitive fields that are ignored by the mapper
        if (trip.getStartTime() != null)
            tripEntity.setStartTime(trip.getStartTime());
        if (trip.getEndTime() != null)
            tripEntity.setEndTime(trip.getEndTime());
        if (trip.getStatus() != null)
            tripEntity.setStatus(trip.getStatus());
        if (trip.getPricingStrategy() != null)
            tripEntity.setPricingStrategy(PricingStrategyConverter.toString(trip.getPricingStrategy()));

        TripEntity entity = jpaTripRepository.save(tripEntity);
        return tripMapper.toDomain(entity);
    }
}
