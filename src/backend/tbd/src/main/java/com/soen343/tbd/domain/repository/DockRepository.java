package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.ids.DockId;

import java.util.Optional;
import java.util.List;

public interface DockRepository {
    Optional<Dock> findById(DockId dockId);

    void save(Dock dock);

    List<Dock> findAll();

    void saveAll(List<Dock> docks);
}

