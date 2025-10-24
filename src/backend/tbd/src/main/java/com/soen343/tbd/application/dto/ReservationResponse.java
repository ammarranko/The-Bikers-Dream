package com.soen343.tbd.application.dto;

public class ReservationResponse {

    private String message;       // A message indicating the result of the reservation request
    private Long reservationId;   // The ID of the created reservation (if successful)

    // Constructor for successful reservation
    public ReservationResponse(String message, Long reservationId) {
        this.message = message;
        this.reservationId = reservationId;
    }

    // Constructor for error or failure message
    public ReservationResponse(String message) {
        this.message = message;
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
}
