package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.StationId;

import java.util.Optional;
import java.util.List;

public interface StationRepository {
    Optional<Station> findById(StationId stationId);

    List<Station> findAll();

    void save(Station station);
}

