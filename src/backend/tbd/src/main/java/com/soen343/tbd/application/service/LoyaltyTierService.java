package com.soen343.tbd.application.service;

import com.soen343.tbd.domain.model.enums.TierType;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoyaltyTierService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    // calls the isEligible method of each tier and checks for eligibility
    public TierType calculateUserTier(User user) {
        // Check from highest to lowest tier
        if (TierType.GOLD.getTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.GOLD;
        }
        if (TierType.SILVER.getTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.SILVER;
        }
        if (TierType.BRONZE.getTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.BRONZE;
        }
        return TierType.NONE;
    }

    // This service method updated the Tier for a given user
    public void updateUserTier(User user) {
        TierType newTier = calculateUserTier(user);
        TierType currentTier = user.getTier();
        // only update if tier has changed
        if (currentTier == null || currentTier != newTier) {
            user.setTier(newTier);
            userRepository.save(user);
            System.out.println("operation completed ");
        }
    }
}

