package com.soen343.tbd.domain.model.user.loyalty;

import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;

// Interface for different loyalty tiers
// each tier will implement its own eligibility criteria, discount rates, and extra reservation times
public interface LoyaltyTier {
    String getTierName();
    double getDiscountRate();
    int getExtraReservationTime();
    boolean isEligible(User user, TripRepository tripRepository, ReservationRepository reservationRepository);

}
