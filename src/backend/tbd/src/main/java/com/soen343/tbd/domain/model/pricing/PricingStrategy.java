package com.soen343.tbd.domain.model.pricing;

public interface PricingStrategy {
    double calculateCost(double durationInMinutes);
    double getBaseFee();
    double getPerMinuteRate();
    String getPricingTypeName();
}
