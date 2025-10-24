package com.soen343.tbd.domain.repository;

import com.soen343.tbd.domain.model.Reservation;
import com.soen343.tbd.domain.model.ids.ReservationId;
import com.soen343.tbd.domain.model.ids.UserId;

import java.util.Optional;

public interface ReservationRepository {
    Optional<Reservation> checkActiveReservationByUserId(UserId userId);

    Optional<Reservation> findById(ReservationId reservationId);

    void save(Reservation reservation);
}
