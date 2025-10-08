package com.soen343.tbd.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data               // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with all arguments
public class SignupRequest {
    private String fullName;
    private String username;
    private String email;
    private String password;
    private String address;
}
