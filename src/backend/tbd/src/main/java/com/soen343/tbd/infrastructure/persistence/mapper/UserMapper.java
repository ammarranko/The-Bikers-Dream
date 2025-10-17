package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.Operator;
import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { BillMapper.class, TripMapper.class })
public abstract class UserMapper {

    @Autowired
    protected BillMapper billMapper;

    @Autowired
    protected TripMapper tripMapper;

    // Entity -> Domain
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserId userId = new UserId(entity.getUserId());

        if ("RIDER".equals(entity.getRole())) {
            // Convert BillEntity -> Bill
            List<Bill> bills = entity.getBills() != null
                    ? entity.getBills().stream()
                        .map(billMapper::toDomain)
                        .collect(Collectors.toList())
                    : null;

            // Convert TripEntity -> Trip
            List<Trip> trips = entity.getTrips() != null
                    ? entity.getTrips().stream()
                        .map(tripMapper::toDomain)
                        .collect(Collectors.toList())
                    : null;

            return new Rider(
                    userId,
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPassword(),
                    entity.getAddress(),
                    entity.getUsername(),
                    entity.getCreatedAt(),
                    null, // payment info (mapped later)
                    bills,
                    trips
            );
        } else if ("OPERATOR".equals(entity.getRole())) {
            return new Operator(
                    userId,
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPassword(),
                    entity.getAddress(),
                    entity.getUsername(),
                    entity.getCreatedAt()
            );
        }

        throw new IllegalArgumentException("Unknown role: " + entity.getRole());
    }

    // Domain -> Entity
    @Mapping(target = "userId", expression = "java(domain.getUserId() != null ? domain.getUserId().value() : null)")
    @Mapping(target = "updatedAt", expression = "java(new java.sql.Timestamp(System.currentTimeMillis()))")
    // Ignore nested relationships since they're mapped separately
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "trips", ignore = true)
    public abstract UserEntity toEntity(User domain);

}
