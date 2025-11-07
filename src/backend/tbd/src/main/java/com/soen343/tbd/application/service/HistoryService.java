package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BikeRepository;
import com.soen343.tbd.domain.repository.BillRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistoryService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private BikeRepository bikeRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private UserRepository userRepository;


    // Get a trip by trip ID and user email (not used for now, as I changed my implementation decision mid-way, but keeping it in case it's needed later)

    public Optional<Trip> getTripByTripIdAndEmail(Long tripId, String email) {
        TripId domainTripId = new TripId(tripId);

        return tripRepository.findByTripIdAndEmail(domainTripId, email);
    }

     //Get all trips for a user by email

    public List<Trip> getAllTripsByEmail(String email) {

        return tripRepository.findTripByEmail(email);
    }

    // Get all trips (for operators)
    public List<Trip> getAllTrips() {
        return tripRepository.findAllTrips();
    }

    public Optional<Bike> getBikeById(Long bikeId) {
        BikeId domainBikeId = new BikeId(bikeId);

        return bikeRepository.findById(domainBikeId);
    }

    public Optional<Bill> getBillById(Long billId) {
        BillId domainBillId = new BillId(billId);
        return billRepository.findById(domainBillId);
    }

    public Optional<User> findUserById(Long userId) {
        UserId domainUserId = new UserId(userId);
        return userRepository.findById(domainUserId);
    }
}

