package com.soen343.tbd.application.dto;

import com.soen343.tbd.domain.model.ids.TripId;

public class RentResponse {
    private Long tripId;

    public RentResponse(TripId tripId){
        this.tripId = tripId.value();
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }
}
