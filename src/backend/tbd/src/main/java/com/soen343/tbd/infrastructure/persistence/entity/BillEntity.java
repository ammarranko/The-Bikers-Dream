package com.soen343.tbd.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bills")
public class BillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "cost")
    private Double cost;

    @OneToOne
    @JoinColumn(name = "trip_id")
    private TripEntity trip;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    
    /* 
    -----------------------
      GETTERS AND SETTERS 
    -----------------------
    */

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public TripEntity getTrip() {
        return trip;
    }

    public void setTrip(TripEntity trip) {
        this.trip = trip;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}
