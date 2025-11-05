package com.soen343.tbd.domain.model.user;

import com.soen343.tbd.domain.model.ids.UserId;

import java.sql.Timestamp;

public class Rider extends User {
    private String paymentInfo;     // Verify implementation for payment

    public Rider(UserId userId, String fullName, String email, String password,
                 String address, String username, Timestamp createdAt, String paymentInfo) {
        super(userId, fullName, email, password, address, username, "RIDER", createdAt);
        this.paymentInfo = paymentInfo;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }
}
