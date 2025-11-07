package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.dto.billing.AllBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserPaymentRequest;
import com.soen343.tbd.application.service.BillingService;
import com.soen343.tbd.application.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    @Autowired
    private BillingService billingService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Get the billing history for the currently authenticated user.
     */
    @GetMapping("/user/history")
    public ResponseEntity<UserBillingHistoryResponse> getUserBillingHistory() {
        logger.info("Received request to get user billing history");

        // Get the current authenticated user's email from JWT token
        String userEmail = currentUserService.getCurrentUserEmail();

        if (userEmail == null) {
            logger.warn("Unauthorized access attempt - no user email in token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserBillingHistoryResponse response = billingService.getAllBillingHistoryForUser(userEmail);
            logger.info("Successfully retrieved billing history for user: {}", userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve billing history for user: {}. Error: {}", userEmail, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/operator/allhistory")
    public ResponseEntity<AllBillingHistoryResponse> getAllSystemBillingHistory() {
        logger.info("Received request to get all system billing history");

        try {
            AllBillingHistoryResponse response = billingService.getAllSystemBillingHistory();
            logger.info("Successfully retrieved all system billing history");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to retrieve all system billing history. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/user/payment")
    public ResponseEntity<Map<String, Object>> processBillPayment(@RequestBody UserPaymentRequest paymentRequest) {
        logger.info("Received request to process bill payment for bill ID: {}", paymentRequest.billId());

        // Get the current authenticated user's email from JWT token
        String userEmail = currentUserService.getCurrentUserEmail();

        if (userEmail == null) {
            logger.warn("Unauthorized payment attempt - no user email in token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            logger.debug("Processing payment for user: {} for bill ID: {}", userEmail, paymentRequest.billId());
            boolean success = billingService.processPayment(paymentRequest, userEmail);

            if (success) {
                logger.info("Payment processed successfully for bill ID: {}", paymentRequest.billId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                response.put("billId", paymentRequest.billId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Payment processing returned false for bill ID: {}", paymentRequest.billId());
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Payment processing failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to process payment for bill ID: {}. Error: {}", paymentRequest.billId(), e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}

