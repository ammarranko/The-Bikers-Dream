package com.soen343.tbd.domain.repository;

import java.util.List;
import java.util.Optional;

import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public interface TripRepository {
    Optional<Trip> checkRentalsByUserId(UserId userId);

    Optional<Trip> findById(TripId tripId);
    
    Optional<Trip> findByTripIdAndEmail(TripId tripId, String email);

    List<Trip> findTripByEmail(String email);

    List<Trip> findAllTrips();

    Trip save(Trip trip);

    List<Trip> findAllByUserId(UserId userId);

    List<Trip> findAll();
}
