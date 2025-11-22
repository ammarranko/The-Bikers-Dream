package com.soen343.tbd.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.infrastructure.persistence.entity.BillEntity;
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

    @Override
    public List<Bill> findAll(List<Long> ids) {
        return jpaBillRepository.findAllById(ids)
                .stream()
                .map(billMapper::toDomain)
                .toList();
    }

    @Override
    public List<Bill> saveAll(List<Bill> bills) {
        var billEntities = bills.stream()
                .map(billMapper::toEntity)
                .toList();

        List<BillEntity> savedEntities = jpaBillRepository.saveAll(billEntities);
        return savedEntities.stream()
                .map(billMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteAll() {
        jpaBillRepository.deleteAll();
    }

    @Override
    public void deleteAllInBatch() {
        jpaBillRepository.deleteAllInBatch();
    }
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
    public Bill save(Bill bill) {
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

        BillEntity entity = jpaBillRepository.save(billEntity);
        return billMapper.toDomain(entity);
    }

    @Override
    public List<Bill> findAllByUserId(UserId userId) {
        return jpaBillRepository.findAllByUser_UserId(userId.value())
                .stream()
                .map(billMapper::toDomain)
                .toList();
    }

    @Override
    public List<Bill> findAll() {
        return jpaBillRepository.findAll()
                .stream()
                .map(billMapper::toDomain)
                .toList();
    }
}
