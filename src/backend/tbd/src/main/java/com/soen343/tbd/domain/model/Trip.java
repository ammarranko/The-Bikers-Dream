package com.soen343.tbd.domain.model;

import java.security.Timestamp;

import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.ids.TripId;
import com.soen343.tbd.domain.model.ids.UserId;

public class Trip {
    private TripId tripId;
    private TripStatus status;
    private BikeId bikeId;
    private UserId userId;
    private StationId startStationId;
    private StationId endStationId;
    private Timestamp startTime;
    private Timestamp endTime;
    private BillId billId;

    public Trip(TripId tripId, BikeId bikeId, UserId userId, StationId startStationId){
        this.tripId = tripId;
        this.status = TripStatus.ONGOING;
        this.bikeId = bikeId;
        this.userId = userId;
        this.startStationId = startStationId;
        this.endStationId = null;
    }
}
