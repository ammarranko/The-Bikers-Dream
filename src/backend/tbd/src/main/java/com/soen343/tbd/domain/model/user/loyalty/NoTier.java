package com.soen343.tbd.domain.model.user.loyalty;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;

public class NoTier implements LoyaltyTier {
    private static final String TIER_NAME = "NONE";
    private static final double DISCOUNT_RATE = 0.0;
    private static final int EXTRA_RESERVATION_TIME = 0;

    @Override
    public String getTierName() {
        return TIER_NAME;
    }

    @Override
    public double getDiscountRate() {
        return DISCOUNT_RATE;
    }

    @Override
    public int getExtraReservationTime() {
        return EXTRA_RESERVATION_TIME;
    }
    // Default for everyone
    @Override
    public boolean isEligible(User user, TripRepository tripRepository, ReservationRepository reservationRepository) {
        return true;
    }
}

