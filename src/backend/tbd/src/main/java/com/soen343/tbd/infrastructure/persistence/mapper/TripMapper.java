package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", imports = {TripId.class, BikeId.class, UserId.class, StationId.class, BillId.class})
public interface TripMapper {

    // ---------------------------
    // Entity TO Domain
    // ---------------------------
    @Mapping(target = "tripId", expression = "java(e.getTripId() != null ? new TripId(e.getTripId()) : null)")
    @Mapping(target = "bikeId", expression = "java(e.getBike() != null && e.getBike().getBikeId() != null ? new BikeId(e.getBike().getBikeId()) : null)")
    @Mapping(target = "userId", expression = "java(e.getUser() != null && e.getUser().getUserId() != null ? new UserId(e.getUser().getUserId()) : null)")
    @Mapping(target = "startStationId", expression = "java(e.getStartStation() != null && e.getStartStation().getStationId() != null ? new StationId(e.getStartStation().getStationId()) : null)")
    @Mapping(target = "endStationId", expression = "java(e.getEndStation() != null && e.getEndStation().getStationId() != null ? new StationId(e.getEndStation().getStationId()) : null)")
    @Mapping(target = "billId", expression = "java(e.getBill() != null && e.getBill().getBillId() != null ? new BillId(e.getBill().getBillId()) : null)")
    Trip toDomain(TripEntity e);


    // ---------------------------
    // Domain TO Entity
    // ---------------------------
    @Mapping(target = "tripId", expression = "java(d.getTripId() != null ? d.getTripId().value() : null)")
    @Mapping(target = "bike", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "startStation", ignore = true)
    @Mapping(target = "endStation", ignore = true)
    @Mapping(target = "bill", ignore = true)
    TripEntity toEntity(Trip d);
}