package com.soen343.tbd.domain.model.user.loyalty;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;

import java.time.LocalDateTime;

public class GoldTier implements LoyaltyTier {
    private static final String TIER_NAME = "GOLD";
    private static final double DISCOUNT_RATE = 0.15;
    private static final int EXTRA_RESERVATION_TIME = 5;

    private final SilverTier silverTier = new SilverTier();

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

    @Override
    public boolean isEligible(User user, TripRepository tripRepository, ReservationRepository reservationRepository) {
       // first check if the user meets Silver tier eligibility
        if (!silverTier.isEligible(user, tripRepository, reservationRepository)) {
            return false;
        }

        // Rider surpasses 5 trips every week for the last 3 months

        LocalDateTime now = LocalDateTime.now();
        int totalWeeks = 12; //3 months = 12 weeks
        for (int i = 0; i < totalWeeks; i++) {
            LocalDateTime weekEnd = now.minusWeeks(i);
            LocalDateTime weekStart = weekEnd.minusWeeks(1);

            int tripsThisWeek = tripRepository.countTripsForUserBetween(user.getUserId(), weekStart, weekEnd);
            // reject whenever a week has less than 5 trips
            if (tripsThisWeek < 5) {
                return false;
            }
        }

        return true;
    }
}

