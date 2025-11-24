package com.soen343.tbd.infrastructure.persistence.adapter;

import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.infrastructure.persistence.mapper.StationMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaStationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public class StationRepositoryAdapter implements StationRepository {
    private final JpaStationRepository jpaStationRepository;
    private final StationMapper stationMapper;

    public StationRepositoryAdapter(JpaStationRepository jpa, StationMapper mapper) {
        this.jpaStationRepository = jpa;
        this.stationMapper = mapper;
    }

    @Override
    public Optional<Station> findById(StationId stationId) {
        return jpaStationRepository.findById(stationId.value())
                .map(stationMapper::toDomain);
    }

    @Override
    public Station save(Station station) {
        var stationEntity = stationMapper.toEntity(station);
        return stationMapper.toDomain(jpaStationRepository.save(stationEntity));
    }

    @Override
    public List<Station> findAll() {
        return jpaStationRepository.findAll()
            .stream()
            .map(stationMapper::toDomain)
            .toList();
    }
}
