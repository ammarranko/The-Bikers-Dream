package com.soen343.tbd.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.repository.BillRepository;
import com.soen343.tbd.infrastructure.persistence.entity.TripEntity;
import com.soen343.tbd.infrastructure.persistence.entity.UserEntity;
import com.soen343.tbd.infrastructure.persistence.mapper.BillMapper;
import com.soen343.tbd.infrastructure.persistence.repository.JpaBillRepository;

import jakarta.persistence.EntityManager;

@Repository
public class BillRepositoryAdapter implements BillRepository {
    private final JpaBillRepository jpaBillRepository;
    private final BillMapper billMapper;
    private final EntityManager entityManager;

    public BillRepositoryAdapter(JpaBillRepository jpa, BillMapper mapper, EntityManager entityManager){
        this.jpaBillRepository = jpa;
        this.billMapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Bill> findById(BillId billId) {
        return jpaBillRepository.findById(billId.value())
                .map(billMapper::toDomain);
    }

    @Override
    public void save(Bill bill) {
        var billEntity = billMapper.toEntity(bill);

        // Set the trip relationship if tripId is present
        if (bill.getTripId() != null) {
            TripEntity tripReference = entityManager.getReference(TripEntity.class, bill.getTripId().value());
            billEntity.setTrip(tripReference);
        }

        // Set the user relationship if userId is present
        if (bill.getUserId() != null) {
            UserEntity userReference = entityManager.getReference(UserEntity.class, bill.getUserId().value());
            billEntity.setUser(userReference);
        }

        jpaBillRepository.save(billEntity);
    }
}
