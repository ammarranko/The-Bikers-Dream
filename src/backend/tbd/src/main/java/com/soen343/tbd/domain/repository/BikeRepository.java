package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.enums.BikeStatus;

import java.util.Optional;
import java.util.List;

public interface BikeRepository {
    Optional<Bike> findById(BikeId bikeId);

    void save(Bike bike);

    Optional<Bike> findByDockId(DockId dockId);

    List<Bike> findByStatus(BikeStatus status);
}
