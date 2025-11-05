package com.soen343.tbd.domain.repository;

import java.util.Optional;

import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.ids.BillId;

public interface BillRepository {
    Optional<Bill> findById(BillId billId);

    Bill save(Bill bill);
}
