package com.soen343.tbd.infrastructure.persistence.adapter;

import com.soen343.tbd.application.service.StationService;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;
import com.soen343.tbd.infrastructure.persistence.mapper.DockMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaDockRepository;

import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.soen343.tbd.infrastructure.persistence.entity.DockEntity;

@Repository
public class DockRepositoryAdapter implements DockRepository {
    private final JpaDockRepository jpaDockRepository;
    private final DockMapper dockMapper;
    private final EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(DockRepositoryAdapter.class);

    public DockRepositoryAdapter(JpaDockRepository jpa, DockMapper mapper, EntityManager entityManager) {
        this.jpaDockRepository = jpa;
        this.dockMapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Dock> findById(DockId dockId) {
        return jpaDockRepository.findById(dockId.value())
                .map(dockMapper::toDomain);
    }

    @Override
    public void save(Dock dock) {
        DockEntity existingDockEntity = jpaDockRepository.findById(dock.getDockId().value())
            .orElseThrow(() -> new IllegalArgumentException("Dock not found: " + dock.getDockId().value()));

        // Only update the status (since station should remain untouched)
        existingDockEntity.setStatus(dock.getStatus());

        // Save the managed entity (will not nullify station_id)
        jpaDockRepository.save(existingDockEntity);
    }
  

}
