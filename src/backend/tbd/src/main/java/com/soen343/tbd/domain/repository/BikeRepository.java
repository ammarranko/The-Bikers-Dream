package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.bike.Bike;
import com.soen343.tbd.domain.model.ids.BikeId;

import java.util.Optional;

public interface BikeRepository {
    Optional<Bike> findById(BikeId bikeId);

    void save(Bike bike);
}
