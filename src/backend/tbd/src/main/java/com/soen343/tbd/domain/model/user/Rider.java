package com.soen343.tbd.domain.model.user;

import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.ids.UserId;

import java.sql.Timestamp;
import java.util.List;

public class Rider extends User {
    private String paymentInfo;     // Verify implementation for payment
    private List<Bill> bills;
    private List<Trip> trips;

    public Rider(UserId userId, String fullName, String email, String password,
                 String address, String username, Timestamp createdAt, String paymentInfo,
                List<Bill> bills, List<Trip> trips) {
        super(userId, fullName, email, password, address, username, "RIDER", createdAt);
        this.paymentInfo = paymentInfo;
        this.bills = bills;
        this.trips = trips;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }
}
