package com.soen343.tbd.application.dto.billing;

public record UserPaymentRequest(
        Long billId,
        String cardHolderName,
        String cardNumber,
        String expiryMonth,
        String expiryYear,
        String cvc
) {
}

