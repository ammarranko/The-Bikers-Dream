package com.soen343.tbd.application.dto;

public class ReservationRequest {

    private Long bikeId; // for creation reservation
    private Long stationId; // for creation reservation
    private Long reservationId; // for cancel reservation
    private String userEmail;  // for creation reservation

    // --------------------
    // Getters and Setters
    // --------------------
    public Long getBikeId() {
        return bikeId;
    }

    public void setBikeId(Long bikeId) {
        this.bikeId = bikeId;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
