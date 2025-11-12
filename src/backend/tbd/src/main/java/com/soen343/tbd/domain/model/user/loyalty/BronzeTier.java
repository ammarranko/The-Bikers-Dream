package com.soen343.tbd.domain.model.user.loyalty;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;

import java.time.LocalDateTime;

public class BronzeTier implements LoyaltyTier  {
    private static final String TIER_NAME = "BRONZE";
    private static final double DISCOUNT_RATE = 0.05; // 5% discount
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

    @Override
    public boolean isEligible(User user, TripRepository tripRepository, ReservationRepository reservationRepository) {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        // a Rider has to have no missed reservations within the last year
        int missedReservationWithinLastYear = reservationRepository.missedReservationscount(user.getUserId(), oneYearAgo);
        if (missedReservationWithinLastYear > 0) {
            return false;
        }

        //  Rider returned all bikes that they ever took successfully
        // reminder: this method checks for all trips with ONGOING state
        int unreturnedBikes = tripRepository.countUnreturnedBikesByUser(user.getUserId());
        if (unreturnedBikes > 0) {
            return false;
        }

 // Rider has surpassed 10 trips in the last year
        int completedTripsWithinLastYear = tripRepository.countTripsForUserByIdSince(user.getUserId(), oneYearAgo);
        return completedTripsWithinLastYear >= 10;
    }
}
