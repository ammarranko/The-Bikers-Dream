package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.ids.DockId;

import java.util.Optional;

public interface DockRepository {
    Optional<Dock> findById(DockId dockId);

    Dock save(Dock dock);
}

