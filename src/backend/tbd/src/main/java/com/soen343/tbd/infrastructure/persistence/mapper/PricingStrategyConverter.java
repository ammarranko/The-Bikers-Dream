package com.soen343.tbd.infrastructure.persistence.mapper;

import com.soen343.tbd.domain.model.pricing.EBikePricing;
import com.soen343.tbd.domain.model.pricing.PricingStrategy;
import com.soen343.tbd.domain.model.pricing.StandardBikePricing;

public class PricingStrategyConverter {

    /**
     * Converts a String (class name) to a PricingStrategy instance.
     */
    public static PricingStrategy fromString(String strategyName) {
        if (strategyName == null || strategyName.isEmpty()) {
            return new StandardBikePricing(); // Default strategy
        }

        switch (strategyName) {
            case "StandardBikePricing":
                return new StandardBikePricing();
            case "EBikePricing":
                return new EBikePricing();
            default:
                return new StandardBikePricing(); // Default fallback
        }
    }

    /**
     * Converts a PricingStrategy instance to its String representation (class name).
     */
    public static String toString(PricingStrategy strategy) {
        if (strategy == null) {
            return "StandardBikePricing"; // Default
        }
        return strategy.getClass().getSimpleName();
    }
}

