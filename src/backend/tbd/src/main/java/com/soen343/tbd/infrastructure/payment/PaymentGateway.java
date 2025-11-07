package com.soen343.tbd.infrastructure.payment;

import com.soen343.tbd.application.dto.billing.UserPaymentRequest;
import com.soen343.tbd.domain.model.user.User;
import org.springframework.stereotype.Component;

/**
 * Dummy external payment gateway that simulates payment processing
 */
@Component
public class PaymentGateway {

    /**
     * Simulates processing a payment through an external payment provide
     */
    public boolean processPayment(UserPaymentRequest paymentRequest, User user, Double amount) {
        // Simulate external payment processing
        System.out.println("[External Payment API] Initiating payment request...");
        System.out.println("[External Payment API] Validating payment credentials...");

        // Validate payment information matches stored user data
        if (!paymentRequest.cardNumber().equals(user.getCardNumber())) {
            System.out.println("[External Payment API] ERROR: Card number validation failed");
            return false;
        }
        if (!paymentRequest.cardHolderName().equals(user.getCardHolderName())) {
            System.out.println("[External Payment API] ERROR: Cardholder name validation failed");
            return false;
        }
        if (!paymentRequest.expiryMonth().equals(user.getExpiryMonth())) {
            System.out.println("[External Payment API] ERROR: Expiry date validation failed");
            return false;
        }
        if (!paymentRequest.expiryYear().equals(user.getExpiryYear())) {
            System.out.println("[External Payment API] ERROR: Expiry date validation failed");
            return false;
        }
        if (!paymentRequest.cvc().equals(user.getCvc())) {
            System.out.println("[External Payment API] ERROR: Security code validation failed");
            return false;
        }

        System.out.println("[External Payment API] Payment credentials validated successfully");
        System.out.println("[External Payment API] Processing transaction for: " + paymentRequest.cardHolderName());
        System.out.println("[External Payment API] Transaction amount: $" + String.format("%.2f", amount));
        System.out.println("[External Payment API] Contacting issuing bank...");
        System.out.println("[External Payment API] Transaction approved - Payment ID: TXN-" + System.currentTimeMillis());

        return true;
    }
}

