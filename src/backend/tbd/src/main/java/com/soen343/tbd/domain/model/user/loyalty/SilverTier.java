package com.soen343.tbd.domain.model.user.loyalty;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;

import java.time.LocalDateTime;

public class SilverTier implements LoyaltyTier {
    private static final String TIER_NAME = "SILVER";
    private static final double DISCOUNT_RATE = 0.10;
    private static final int EXTRA_RESERVATION_TIME = 2;

    private final BronzeTier bronzeTier = new BronzeTier();

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
        // Rider covers Bronze tier eligibility
        if (!bronzeTier.isEligible(user, tripRepository, reservationRepository)) {
            return false;
        }

        // At least 5 reservations successfully claimed in last year
        // Rider has surpassed 5 trips per month for the last three months
        // Check each 30-day period going backwards from today
        LocalDateTime now = LocalDateTime.now();

        // here we check on an interval of 30 days, starting from today and backward
        for (int i = 0; i < 3; i++) {
            LocalDateTime periodEnd = now.minusDays(i * 30L);
            LocalDateTime periodStart = periodEnd.minusDays(30);

            int tripsThisPeriod = tripRepository.countTripsForUserBetween(user.getUserId(), periodStart, periodEnd);
            // reject whenever a month has less than 5 trips
            if (tripsThisPeriod < 5) {
                return false;
            }
        }

        return true;
    }
}

