package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.pricing.StandardBikePricing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.soen343.tbd.domain.model.enums.BillStatus;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;
import java.sql.Timestamp;

@ExtendWith(MockitoExtension.class)
public class BillTest {
    @Test
    void calculateCostTest_ComputesExpectedCost() {
        Trip trip = mock(Trip.class);
        PricingStrategy pricingStrategy = new StandardBikePricing();
        TripId tripId = new TripId(1L);
        UserId userId = new UserId(2L);
        when(trip.getTripId()).thenReturn(tripId);
        when(trip.getUserId()).thenReturn(userId);
        when(trip.getStartTime()).thenReturn(new Timestamp(0));
        when(trip.getEndTime()).thenReturn(new Timestamp(60000));
        when(trip.getPricingStrategy()).thenReturn(pricingStrategy);
        when(trip.calculateDurationInMinutes()).thenReturn(60.0);

        // StandardBikePricing: cost = 1.0 + (60 * 0.5) = 31.0
        Bill bill = new Bill(trip, 0.25); // 25% discount

        assertThat(bill.getRegularCost()).isEqualTo(31.0);
        assertThat(bill.getDiscountedCost()).isEqualTo(23.25);
    }
}
