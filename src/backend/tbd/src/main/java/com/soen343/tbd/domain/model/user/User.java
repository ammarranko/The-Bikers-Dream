package com.soen343.tbd.domain.model.user;

import com.soen343.tbd.domain.model.ids.UserId;

import java.sql.Timestamp;
import java.util.Objects;

public abstract class User {
    private final UserId userId;
    private String fullName;
    private String email;
    private String password;
    private String address;
    private String username;
    private String role;
    private Timestamp createdAt;
    private String cardHolderName;
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvc;
    private Integer flexmoney;

    public User(UserId userId, String fullName, String email, String password,
                String address, String username, String role, Timestamp createdAt, Integer flexmoney) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.role = role;
        this.createdAt = createdAt;
        this.username = username;
        this.flexmoney = flexmoney;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public Integer getFlexMoney() {
        return flexmoney;
    }

    public void setFlexMoney(Integer flexmoney) {
        this.flexmoney = flexmoney;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) 
        && Objects.equals(fullName, user.fullName) 
        && Objects.equals(email, user.email) 
        && Objects.equals(password, user.password) 
        && Objects.equals(address, user.address) 
        && Objects.equals(username, user.username) 
        && Objects.equals(role, user.role) 
        && Objects.equals(createdAt, user.createdAt) 
        && Objects.equals(flexmoney, user.flexmoney);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, fullName, email, password, address, username, role, createdAt, flexmoney);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", address='" + address + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", flexmoney=" + flexmoney +
                '}';
    }
}
