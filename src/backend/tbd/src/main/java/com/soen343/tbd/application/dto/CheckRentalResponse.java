package com.soen343.tbd.application.dto;

import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class CheckRentalResponse {
    private Long tripId;
    private Long userId;
    private boolean hasOngoingRental;
    private Long bikeId;

    public CheckRentalResponse(TripId tripId, UserId userId, boolean hasOngoingRental, BikeId bikeId) {
        this.tripId = tripId != null ? tripId.value() : null;
        this.userId = userId != null ? userId.value() : null;
        this.hasOngoingRental = hasOngoingRental;
        this.bikeId = bikeId!= null ? bikeId.value() : null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isHasOngoingRental() {
        return hasOngoingRental;
    }

    public void setHasOngoingRental(boolean hasOngoingRental) {
        this.hasOngoingRental = hasOngoingRental;
    }

    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
