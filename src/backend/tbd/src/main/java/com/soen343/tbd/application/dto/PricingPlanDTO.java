package com.soen343.tbd.application.dto;

public class PricingPlanDTO {
    private String planName;
    private double baseFee;
    private double perMinuteRate;
    private double eBikeSurcharge;

    public PricingPlanDTO() {}

    public PricingPlanDTO(String planName, double baseFee, double perMinuteRate, double eBikeSurcharg) {
        this.planName = planName;
        this.baseFee = baseFee;
        this.perMinuteRate = perMinuteRate;
        this.eBikeSurcharge = eBikeSurcharge;
    }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public double getBaseFee() { return baseFee; }
    public void setBaseFee(double baseFee) { this.baseFee = baseFee; }

    public double getPerMinuteRate() { return perMinuteRate; }
    public void setPerMinuteRate(double perMinuteRate) { this.perMinuteRate = perMinuteRate; }

    public double getEBikeSurcharge() { return eBikeSurcharge; }
    public void setEBikeSurcharge(double eBikeSurcharge) { this.eBikeSurcharge = eBikeSurcharge; }
}
