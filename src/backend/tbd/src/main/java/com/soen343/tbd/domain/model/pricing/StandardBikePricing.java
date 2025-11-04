package com.soen343.tbd.domain.model.pricing;

public class StandardBikePricing implements PricingStrategy {
    private static final String PRICING_TYPE_NAME = "Standard Bike Pricing";
    private static final double BASE_FEE = 1.0;
    private static final double COST_PER_MINUTE = 0.5;

    public double calculateCost(double durationMinutes) {
        return BASE_FEE + (durationMinutes * COST_PER_MINUTE);
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE;
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
