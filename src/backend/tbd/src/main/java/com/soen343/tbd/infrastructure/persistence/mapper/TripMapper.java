package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PricingStrategyConverter.class, imports = { TripId.class, BikeId.class, UserId.class, StationId.class, BillId.class })
public interface TripMapper {

    // ---------------------------
    // Entity TO Domain
    // ---------------------------
    @Mapping(target = "tripId", expression = "java(e.getTripId() != null ? new TripId(e.getTripId()) : null)")
    @Mapping(target = "bikeId", expression = "java(e.getBikeId() != null ? new BikeId(e.getBikeId()) : null)")
    @Mapping(target = "userId", expression = "java(e.getUserId() != null ? new UserId(e.getUserId()) : null)")
    @Mapping(target = "startStationId", expression = "java(e.getStartStationId() != null ? new StationId(e.getStartStationId()) : null)")
    @Mapping(target = "endStationId", expression = "java(e.getEndStationId() != null ? new StationId(e.getEndStationId()) : null)")
    @Mapping(target = "billId", expression = "java(e.getBill() != null && e.getBill().getBillId() != null ? new BillId(e.getBill().getBillId()) : null)")
    @Mapping(target = "pricingStrategy", expression = "java(PricingStrategyConverter.fromString(e.getPricingStrategy()))")
    Trip toDomain(TripEntity e);

    // ---------------------------
    // Domain TO Entity
    // ---------------------------
    @Mapping(target = "tripId", expression = "java(d.getTripId() != null ? d.getTripId().value() : null)")
    @Mapping(target = "bikeId", expression = "java(d.getBikeId() != null ? d.getBikeId().value() : null)")
    @Mapping(target = "userId", expression = "java(d.getUserId() != null ? d.getUserId().value() : null)")
    @Mapping(target = "startStationId", expression = "java(d.getStartStationId() != null ? d.getStartStationId().value() : null)")
    @Mapping(target = "endStationId", expression = "java(d.getEndStationId() != null ? d.getEndStationId().value() : null)")
    @Mapping(target = "bike", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "startStation", ignore = true)
    @Mapping(target = "endStation", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "pricingStrategy", expression = "java(PricingStrategyConverter.toString(d.getPricingStrategy()))")
    TripEntity toEntity(Trip d);
}
