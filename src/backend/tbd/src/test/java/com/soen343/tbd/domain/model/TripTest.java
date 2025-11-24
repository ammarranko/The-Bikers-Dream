package com.soen343.tbd.domain.model;

import com.soen343.tbd.domain.model.pricing.StandardBikePricing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.sql.Timestamp;
import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.*;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;

import static org.assertj.core.api.Assertions.*;

public class TripTest {
    private Trip trip;
    private TripId tripId = new TripId(1L);
    private BikeId bikeId = new BikeId(1L);
    private UserId userId = new UserId(1L);
    private StationId startStationId = new StationId(1L);
    private StationId endStationId = new StationId(2L);
    private PricingStrategy pricingStrategy = new StandardBikePricing();

    @BeforeEach
    void setUp() {
        trip = new Trip(tripId, bikeId, userId, startStationId, pricingStrategy);
    }

    @Test
    void endTripTest_shouldUpdateStatusAndReturnBill() {
        Bill bill = trip.endTrip(endStationId, 0.0);

        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(trip.getEndTime()).isNotNull();
        assertThat(trip.getEndStationId()).isEqualTo(endStationId);
        assertThat(bill).isNotNull();
        assertThat(trip.getBillId()).isEqualTo(bill.getBillId());
    }

    @Test
    void endTripTest_shouldThrowExceptionIfAlreadyCompleted() {
        trip.endTrip(endStationId, 0.0);
        assertThatThrownBy(() -> trip.endTrip(endStationId, 0.0))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void calculateDurationInMinutesTest_shouldReturnCorrectValue() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = new Timestamp(start.getTime() + 90000); // 1.5 minutes

        trip.setStartTime(start);
        trip.setEndTime(end);

        double duration = trip.calculateDurationInMinutes();
        assertThat(duration).isCloseTo(1.5, within(0.01));
    }

    @Test
    void calculateDurationInMinutesTest_shouldReturnZeroIfTimesNull() {
        trip.setStartTime(null);
        trip.setEndTime(null);

        assertThat(trip.calculateDurationInMinutes()).isEqualTo(0.0);
    }
}
