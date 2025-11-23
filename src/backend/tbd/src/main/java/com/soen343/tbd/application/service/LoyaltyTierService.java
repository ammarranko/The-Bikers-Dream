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
        if (TierType.GOLD.getLoyaltyTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.GOLD;
        }
        if (TierType.SILVER.getLoyaltyTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.SILVER;
        }
        if (TierType.BRONZE.getLoyaltyTier().isEligible(user, tripRepository, reservationRepository)) {
            return TierType.BRONZE;
        }
        return TierType.NONE;
    }

    // This service method updated the Tier for a given user
    // Returns true if the tier was updated, false otherwise
    public boolean updateUserTier(User user) {
        TierType newTier = calculateUserTier(user);
        TierType currentTier = user.getTierType();
        // only update if tier has changed
        if (currentTier == null || currentTier != newTier) {
            user.setTierType(newTier);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Resets the user's tier to NONE (e.g. penalty for expired reservation)
    public void resetTierToNone(User user) {
        user.setTierType(TierType.NONE);
        userRepository.save(user);
    }
}

