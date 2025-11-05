package com.soen343.tbd.domain.model.pricing;

public class EBikePricing implements PricingStrategy {
    private static final double BASE_FARE = 3.00;
    private static final double COST_PER_MINUTE = 0.35;

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
}
