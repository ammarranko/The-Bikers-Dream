package com.soen343.tbd.application.dto;

import java.time.LocalDateTime; // for LocalDateTime



public class ReservationResponse {

    private String message;       // A message indicating the result of the reservation request
    private Long reservationId;   // The ID of the created reservation 
    private Long bikeId;
    private boolean hasActiveReservation; // Used for /check only for frontend use
    private Long stationId;
    private LocalDateTime expiresAt;
    private String userTier;      // User's loyalty tier after action

 

    public ReservationResponse(String message, Long reservationId) {
        this.message = message;
        this.reservationId = reservationId;
        this.hasActiveReservation = true; // optional, depending on use case
    }


     // Constructor for /create
    public ReservationResponse(String message, Long reservationId, Long bikeId, Long stationId, LocalDateTime expiresAt) {
        this.message = message;
        this.reservationId = reservationId;
        this.bikeId = bikeId;
        this.stationId = stationId;
        this.expiresAt = expiresAt;
        this.hasActiveReservation = true;
    }

    // Constructor for /check (active reservation exists)
    public ReservationResponse(boolean hasActiveReservation, Long bikeId, Long stationId, LocalDateTime expiresAt) {
        this.hasActiveReservation = hasActiveReservation;
        this.bikeId = bikeId;
        this.stationId = stationId;
        this.expiresAt = expiresAt;
    }

    // Constructor for /check (no active reservation)
    public ReservationResponse(boolean hasActiveReservation) {
        this.hasActiveReservation = hasActiveReservation;
    }

    // Constructor for /cancel or error messages
    public ReservationResponse(String message) {
        this.message = message;
    }

    // Constructor for /end with tier information
    public ReservationResponse(String message, String userTier) {
        this.message = message;
        this.userTier = userTier;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

 
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

    public boolean isHasActiveReservation() {
        return hasActiveReservation;
    }

    public void setHasActiveReservation(boolean hasActiveReservation) {
        this.hasActiveReservation = hasActiveReservation;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getUserTier() {
        return userTier;
    }

    public void setUserTier(String userTier) {
        this.userTier = userTier;
    }
}