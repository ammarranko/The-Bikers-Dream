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
    @Mapping(target = "tripId", expression = "java(e.getTripId() != null ? new TripId(e.getTripId()) : null)")
    @Mapping(target = "userId", expression = "java(e.getUserId() != null ? new UserId(e.getUserId()) : null)")
    @Mapping(target = "cost", source = "cost")
    Bill toDomain(BillEntity e);

    // Domain -> Entity
    @Mapping(target = "billId", expression = "java(d.getBillId() != null ? d.getBillId().value() : null)")
    @Mapping(target = "tripId", expression = "java(d.getTripId() != null ? d.getTripId().value() : null)")
    @Mapping(target = "userId", expression = "java(d.getUserId() != null ? d.getUserId().value() : null)")
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "cost", source = "cost")
    BillEntity toEntity(Bill d);
}