package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.infrastructure.persistence.entity.DockEntity;
import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DockMapper.class, TripMapper.class, StationId.class})
public interface StationMapper {

    // Entity to Domain
    @Mapping(target = "stationId", expression = "java(new StationId(e.getStationId()))")
    @Mapping(target = "numberOfBikesDocked", ignore = true)
    Station toDomain(StationEntity e);

    // Domain to Entity
    @Mapping(target = "stationId", expression = "java(d.getStationId() != null ? d.getStationId().value() : null)")
    StationEntity toEntity(Station d);

    List<Dock> toDomainDockList(List<DockEntity> dockEntityList);
    List<DockEntity> toEntityDockList(List<Dock> dockDomainList);

    List<Trip> toDomainTripList(List<TripEntity> tripEntityList);
    List<TripEntity> toEntityTripList(List<Trip> tripDomainList);
}
