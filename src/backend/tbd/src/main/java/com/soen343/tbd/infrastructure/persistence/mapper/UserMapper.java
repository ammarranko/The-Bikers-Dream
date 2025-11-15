package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.Operator;
import com.soen343.tbd.domain.model.user.Rider;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    // Entity -> Domain
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserId userId = new UserId(entity.getUserId());

        if ("RIDER".equals(entity.getRole())) {
            Rider rider = new Rider(
                    userId,
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPassword(),
                    entity.getAddress(),
                    entity.getUsername(),
                    entity.getCreatedAt(),
                    null, // payment info (mapped later)
                    entity.getFlexMoney()
            );

            // Set payment information
            rider.setCardHolderName(entity.getCardHolderName());
            rider.setCardNumber(entity.getCardNumber());
            rider.setExpiryMonth(entity.getExpiryMonth());
            rider.setExpiryYear(entity.getExpiryYear());
            rider.setCvc(entity.getCvc());

            return rider;
        } else if ("OPERATOR".equals(entity.getRole())) {
            Operator operator = new Operator(
                    userId,
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPassword(),
                    entity.getAddress(),
                    entity.getUsername(),
                    entity.getCreatedAt(), 
                    entity.getFlexMoney()
            );

            return operator;
        }

        throw new IllegalArgumentException("Unknown role: " + entity.getRole());
    }

    // Domain -> Entity
    @Mapping(target = "userId", expression = "java(domain.getUserId() != null ? domain.getUserId().value() : null)")
    @Mapping(target = "updatedAt", expression = "java(new java.sql.Timestamp(System.currentTimeMillis()))")
    // Ignore nested relationships since they're never used and cause performance issues
    @Mapping(target = "bills", ignore = true)
    @Mapping(target = "trips", ignore = true)
    public abstract UserEntity toEntity(User domain);

}
