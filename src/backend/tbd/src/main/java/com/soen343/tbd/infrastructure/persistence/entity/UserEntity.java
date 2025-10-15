package com.soen343.tbd.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.List;

import java.sql.Timestamp;

@Entity
@Table(name = "users") // Schema is now handled by configuration
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name") // if column name has underscore
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "address")
    private String address;

    @Column(name = "username")
    private String username;

    @Column(name = "role")
    private String role;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @OneToMany(mappedBy="user", fetch = FetchType.LAZY)
    List<BillEntity> bills;

    @OneToMany(mappedBy="user", fetch = FetchType.LAZY)
    List<TripEntity> trips;


    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.createdAt = created_at;
    }

    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<BillEntity> getBills() {
        return bills;
    }

    public void setBills(List<BillEntity> bills) {
        this.bills = bills;
    }

    public List<TripEntity> getTrips() {
        return trips;
    }

    public void setTrips(List<TripEntity> trips) {
        this.trips = trips;
    }

}