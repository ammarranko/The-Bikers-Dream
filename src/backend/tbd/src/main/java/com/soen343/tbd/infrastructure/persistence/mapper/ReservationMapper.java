package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.infrastructure.persistence.entity.ReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {ReservationId.class, BikeId.class, UserId.class, StationId.class})
public interface ReservationMapper {

    // ---------------------------
    // Entity → Domain
    // ---------------------------
    @Mapping(target = "reservationId", expression = "java(e.getReservationId() != null ? new ReservationId(e.getReservationId()) : null)")
    @Mapping(target = "bikeId", expression = "java(e.getBike() != null && e.getBike().getBikeId() != null ? new BikeId(e.getBike().getBikeId()) : null)")
    @Mapping(target = "userId", expression = "java(e.getUser() != null && e.getUser().getUserId() != null ? new UserId(e.getUser().getUserId()) : null)")
    @Mapping(target = "startStationId", expression = "java(e.getStartStation() != null && e.getStartStation().getStationId() != null ? new StationId(e.getStartStation().getStationId()) : null)")
    @Mapping(target = "reservedAt", source = "reservedAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "status", source = "status")
    Reservation toDomain(ReservationEntity e);

    // ---------------------------
    // Domain → Entity
    // ---------------------------
    @Mapping(target = "reservationId", expression = "java(d.getReservationId() != null ? d.getReservationId().value() : null)")
    @Mapping(target = "bike", ignore = true)          // will be set in service layer
    @Mapping(target = "user", ignore = true)          // will be set in service layer
    @Mapping(target = "startStation", ignore = true)  // will be set in service layer
    @Mapping(target = "reservedAt", source = "reservedAt")
    @Mapping(target = "expiresAt", source = "expiresAt")
    @Mapping(target = "status", source = "status")
    ReservationEntity toEntity(Reservation d);
}
