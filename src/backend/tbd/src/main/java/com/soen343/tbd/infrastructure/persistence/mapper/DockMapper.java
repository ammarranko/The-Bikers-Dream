package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.ids.DockId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.infrastructure.persistence.entity.DockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { DockId.class, StationId.class })
public interface DockMapper {

    // Entity to Domain
    @Mapping(target = "dockId", expression = "java(new DockId(e.getDockId()))")
    @Mapping(target = "stationId", expression = "java(e.getStation() != null ? new StationId(e.getStation().getStationId()) : null)")
    Dock toDomain(DockEntity e);

    // Domain to Entity - station relationship is handled separately in the adapter
    @Mapping(target = "dockId", expression = "java(d.getDockId() != null ? d.getDockId().value() : null)")
    @Mapping(target = "station", ignore = true)
    DockEntity toEntity(Dock d);
}

