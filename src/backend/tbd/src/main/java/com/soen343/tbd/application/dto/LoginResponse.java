package com.soen343.tbd.application.dto;

public class LoginResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private String username;
    private String tier;
    private Integer flexMoney;

    public LoginResponse(String token, String email, String fullName, String role, String username, String tier, Integer flexMoney){
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.username = username;
        this.tier = tier;
        this.flexMoney = flexMoney;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTier() {
        return tier;
    }
    public void setTier(String tier) {
        this.tier = tier;
    }

    public Integer getFlexMoney() {
        return flexMoney;
    }

    public void setFlexMoney(Integer flexMoney) {
        this.flexMoney = flexMoney;
    }
}
