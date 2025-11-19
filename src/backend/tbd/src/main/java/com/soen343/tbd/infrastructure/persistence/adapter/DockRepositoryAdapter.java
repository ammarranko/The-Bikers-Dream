package com.soen343.tbd.infrastructure.persistence.adapter;

import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.repository.DockRepository;
import com.soen343.tbd.infrastructure.persistence.mapper.DockMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaDockRepository;

import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.soen343.tbd.infrastructure.persistence.entity.DockEntity;

@Repository
public class DockRepositoryAdapter implements DockRepository {
    private final JpaDockRepository jpaDockRepository;
    private final DockMapper dockMapper;
    private final EntityManager entityManager;
    private final Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(DockRepositoryAdapter.class);

    public DockRepositoryAdapter(JpaDockRepository jpa, DockMapper mapper, EntityManager entityManager, Environment environment) {
        this.jpaDockRepository = jpa;
        this.dockMapper = mapper;
        this.entityManager = entityManager;
        this.environment = environment;
    }

    @Override
    public Optional<Dock> findById(DockId dockId) {
        return jpaDockRepository.findById(dockId.value())
                .map(dockMapper::toDomain);
    }

    @Override
    public Dock save(Dock dock) {
        if (dock.getDockId() == null) {
            // Allow DB to generate dockId
            DockEntity dockEntity = dockMapper.toEntity(dock);
            DockEntity savedEntity = jpaDockRepository.save(dockEntity);
            return dockMapper.toDomain(savedEntity);
        }

        Optional<DockEntity> optionalDockEntity = jpaDockRepository.findById(dock.getDockId().value());
        DockEntity dockEntity;
        if (optionalDockEntity.isPresent()) {
            dockEntity = optionalDockEntity.get();
            dockEntity.setStatus(dock.getStatus());
        } else if (isTestProfileActive()) {
            // Allow creation in test profile
            dockEntity = dockMapper.toEntity(dock);
        } else {
            throw new IllegalArgumentException("Dock not found: " + dock.getDockId().value());
        }
        return dockMapper.toDomain(jpaDockRepository.save(dockEntity));
    }

    private boolean isTestProfileActive() {
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("test")) {
                return true;
            }
        }
        return false;
    }
}
