package com.soen343.tbd.domain.model.pricing;

public class EBikePricing implements PricingStrategy {
    private static final String PRICING_TYPE_NAME = "E-Bike Pricing";
    private static final double BASE_FARE = 3.00;
    private static final double COST_PER_MINUTE = 0.35;

    @Override
    public double calculateCost(double durationInMinutes) {
        return BASE_FARE + (COST_PER_MINUTE * durationInMinutes);
    }

    @Override
    public double getBaseFee() {
        return BASE_FARE;
    }

    @Override
    public double getPerMinuteRate() {
        return COST_PER_MINUTE;
    }
    
    @Override
    public String getPricingTypeName() {
        return PRICING_TYPE_NAME;
    }
}
