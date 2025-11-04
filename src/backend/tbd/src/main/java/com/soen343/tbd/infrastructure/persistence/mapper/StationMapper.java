package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.infrastructure.persistence.entity.StationEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {DockMapper.class, StationId.class})
public interface StationMapper {

    // Entity to Domain
    @Mapping(target = "stationId", expression = "java(new StationId(e.getStationId()))")
    @Mapping(target = "numberOfBikesDocked", ignore = true)
    Station toDomain(StationEntity e);

    // Domain to Entity
    @Mapping(target = "stationId", expression = "java(d.getStationId() != null ? d.getStationId().value() : null)")
    @Mapping(target = "startedTrips", ignore = true)  // Never used
    @Mapping(target = "endedTrips", ignore = true)    // Never used
    StationEntity toEntity(Station d);
}
