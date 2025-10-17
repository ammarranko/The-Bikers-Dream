package com.soen343.tbd.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.infrastructure.persistence.entity.BillEntity;

@Mapper(componentModel = "spring", imports = {BillId.class, TripId.class, UserId.class})
public interface BillMapper {

    // Entity -> Domain
    @Mapping(target = "billId", expression = "java(e.getBillId() != null ? new BillId(e.getBillId()) : null)")
    @Mapping(target = "tripId", expression = "java(e.getTrip() != null && e.getTrip().getTripId() != null ? new TripId(e.getTrip().getTripId()) : null)")
    @Mapping(target = "userId", expression = "java(e.getUser() != null && e.getUser().getUserId() != null ? new UserId(e.getUser().getUserId()) : null)")
    @Mapping(target = "cost", source = "cost")
    Bill toDomain(BillEntity e);

    // Domain -> Entity
    @Mapping(target = "billId", expression = "java(d.getBillId() != null ? d.getBillId().value() : null)")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "cost", source = "cost")
    BillEntity toEntity(Bill d);
}