package com.soen343.tbd.domain.model.enums;

import com.soen343.tbd.domain.model.user.loyalty.*;

public enum TierType {
    NONE(new NoTier()),
    BRONZE(new BronzeTier()),
    SILVER(new SilverTier()),
    GOLD(new GoldTier());

    private final LoyaltyTier tier;

    TierType(LoyaltyTier tier) {
        this.tier = tier;
    }

    public LoyaltyTier getLoyaltyTier() {
        return tier;
    }
}

