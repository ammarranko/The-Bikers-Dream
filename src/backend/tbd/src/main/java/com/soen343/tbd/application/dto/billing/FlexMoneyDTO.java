package com.soen343.tbd.application.dto.billing;

public class FlexMoneyDTO {
    private Integer currentBalance; // current amount of fake money
    private Integer addRemoveMoney; // for adding more fake money, can be negative

    public FlexMoneyDTO(Integer currentBalance, Integer addRemoveMoney) {
        this.currentBalance = currentBalance;
        this.addRemoveMoney = addRemoveMoney;
    }

    /*******************
     
     Getters and setters
     
     ******************/

    public Integer getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Integer getAddRemoveMoney() {
        return addRemoveMoney;
    }

    public void setAddRemoveMoney(Integer addRemoveMoney) {
        this.addRemoveMoney = addRemoveMoney;
    }
    
}
