package com.soen343.tbd.domain.model.user;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data               // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with all arguments

public class User {

    private int id;

    private String fullName;

    private String email;

    private String password;

    private String role;

    private Timestamp created_at;


    private String address;
    private String username;
}


