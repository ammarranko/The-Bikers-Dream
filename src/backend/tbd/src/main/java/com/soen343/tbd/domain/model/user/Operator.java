package com.soen343.tbd.domain.model.user;

import com.soen343.tbd.domain.model.ids.UserId;

import java.sql.Timestamp;

public class Operator extends User{

    public Operator(UserId userId, String fullName, String email, String password,
                    String address, String username, Timestamp createdAt, Integer flexmoney) {
        super(userId, fullName, email, password, address, username, "OPERATOR", createdAt, flexmoney);
    }
}
