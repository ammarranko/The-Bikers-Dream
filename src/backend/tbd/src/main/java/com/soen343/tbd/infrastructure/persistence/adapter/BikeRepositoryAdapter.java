package com.soen343.tbd.infrastructure.persistence.adapter;

import java.util.List;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.enums.BikeStatus;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.infrastructure.persistence.entity.DockEntity;
import com.soen343.tbd.infrastructure.persistence.mapper.BikeMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaBikeRepository;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.soen343.tbd.infrastructure.persistence.entity.BikeEntity;

@Repository
public class BikeRepositoryAdapter implements BikeRepository {
    private final JpaBikeRepository jpaBikeRepository;
    private final BikeMapper bikeMapper;
    private final EntityManager entityManager;

    public BikeRepositoryAdapter(JpaBikeRepository jpa, BikeMapper mapper, EntityManager entityManager) {
        this.jpaBikeRepository = jpa;
        this.bikeMapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Bike> findById(BikeId bikeId) {
        return jpaBikeRepository.findById(bikeId.value())
                .map(b -> bikeMapper.toDomain((BikeEntity) b));
    }

    @Override
    public List<Bike> findAll() {
        return jpaBikeRepository.findAll()
                .stream()
                .map(bikeEntity -> bikeMapper.toDomain((BikeEntity) bikeEntity))
                .toList();
    }

    @Override
    public void saveAll(List<Bike> bikes) {
        for (Bike bike : bikes) {
            save(bike);
        }
    }

    @Override
    public void save(Bike bike) {
        var bikeEntity = bikeMapper.toEntity(bike);

        // Set the dock relationship if dockId is present
        if (bike.getDockId() != null) {
            var dockReference = entityManager.getReference(DockEntity.class, bike.getDockId().value());
            bikeEntity.setDock(dockReference);
        } else {
            bikeEntity.setDock(null);
        }

        jpaBikeRepository.save(bikeEntity);
    }

    @Override
    public Optional<Bike> findByDockId(DockId dockId) {
        return jpaBikeRepository.findByDock_DockId(dockId.value())
                .map(b -> bikeMapper.toDomain((BikeEntity) b));
    }

    @Override
    public List<Bike> findByStatus(BikeStatus status) {
        return jpaBikeRepository.findByStatus(status)
                .stream()
                .map(bikeEntity -> bikeMapper.toDomain((BikeEntity) bikeEntity))
                .toList();
    }

    @Override
    public void flush() {
        jpaBikeRepository.flush();
    }
}
