package com.soen343.tbd.application.service;

import com.soen343.tbd.application.dto.billing.AllBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserBillingHistoryResponse;
import com.soen343.tbd.application.dto.billing.UserPaymentRequest;
import com.soen343.tbd.domain.model.Bill;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.enums.BillStatus;
import com.soen343.tbd.domain.model.ids.BillId;
import com.soen343.tbd.domain.model.ids.UserId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.BillRepository;
import com.soen343.tbd.domain.repository.StationRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import com.soen343.tbd.infrastructure.payment.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final BillRepository billRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final PaymentGateway paymentGateway;
    private final FlexMoneyService flexMoneyService;

    public BillingService(BillRepository billRepository, 
                        TripRepository tripRepository,
                        UserRepository userRepository, 
                        StationRepository stationRepository,
                        PaymentGateway paymentGateway,
                        FlexMoneyService flexMoneyService
                ) {
        this.billRepository = billRepository;
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.paymentGateway = paymentGateway;
        this.flexMoneyService = flexMoneyService;
    }

    @Transactional(readOnly = true)
    public UserBillingHistoryResponse getAllBillingHistoryForUser(String userEmail) {
        logger.info("Starting process to get bill history for user: {}...", userEmail);
        logger.info("Fetching billing history for user: {}", userEmail);

        // Fetch the user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + userEmail));

        // Fetch all the trips associated with the user
        List<Trip> trips = tripRepository.findAllByUserId(user.getUserId());
        logger.debug("Found {} trips for user: {}", trips.size(), userEmail);

        // Fetch all the bills associated with the user
        List<Bill> bills = billRepository.findAllByUserId(user.getUserId());
        logger.debug("Found {} bills for user: {}", bills.size(), userEmail);

        logger.info("Successfully fetched billing history for user: {}", userEmail);
        return createBillingHistoryResponse(trips, bills, user);
    }

    @Transactional(readOnly = true)
    public AllBillingHistoryResponse getAllSystemBillingHistory() {
        logger.info("Starting process to get all system billing history...");

        // Fetch all trips
        List<Trip> allTrips = tripRepository.findAll();
        logger.debug("Found {} total trips in the system", allTrips.size());

        // Fetch all bills
        List<Bill> allBills = billRepository.findAll();
        logger.debug("Found {} total bills in the system", allBills.size());

        logger.info("Successfully fetched all system billing history");
        return createAllSystemBillingHistoryResponse(allTrips, allBills);
    }


    /**
     * Process payment for a bill
     */
    @Transactional
    public boolean processPayment(UserPaymentRequest paymentRequest, String userEmail) {
        logger.info("Starting payment process for bill ID: {} by user: {}...", paymentRequest.billId(), userEmail);

        // Fetch the bill
        Bill bill = billRepository.findById(new BillId(paymentRequest.billId()))
                .orElseThrow(() -> {
                    logger.error("Bill not found with ID: {}", paymentRequest.billId());
                    return new RuntimeException("Bill not found with ID: " + paymentRequest.billId());
                });

        // Verify the bill is still pending
        if (bill.getStatus() == BillStatus.PAID) {
            logger.warn("Attempted to pay already paid bill. Bill ID: {}", paymentRequest.billId());
            throw new RuntimeException("Bill has already been paid");
        }

        // Fetch the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", userEmail);
                    return new RuntimeException("User not found with email: " + userEmail);
                });

        // Verify the bill belongs to this user
        if (!bill.getUserId().equals(user.getUserId())) {
            logger.error("User {} attempted to pay bill {} which belongs to user {}",
                    userEmail, paymentRequest.billId(), bill.getUserId());
            throw new RuntimeException("User does not have permission to pay this bill");
        }

        // Process payment through the payment gateway (which will validate payment information)
        logger.debug("Calling payment gateway for bill ID: {}, amount: ${}", paymentRequest.billId(), bill.getDiscountedCost());
        boolean paymentSuccess = paymentGateway.processPayment(
                paymentRequest,
                user,
                bill.getDiscountedCost()
        );

        if (!paymentSuccess) {
            logger.error("Payment processing failed for bill ID: {}", paymentRequest.billId());
            throw new RuntimeException("Payment processing failed");
        }

        // Update bill status to PAID
        bill.setStatus(BillStatus.PAID);
        billRepository.save(bill);

        logger.info("Payment processed successfully! Bill ID: {} updated to PAID status", paymentRequest.billId());

        return true;
    }

    /**
     * Use flex money to reduce a bill's cost
     */
    @Transactional
    public Bill applyFlexMoney(Bill bill, UserId userId) {

        double originalCost = bill.getDiscountedCost();
        double reducedCost = flexMoneyService.reduceBillWithFlexMoney(userId, originalCost);

        bill.setDiscountedCost(reducedCost);

        return bill;
}

    private UserBillingHistoryResponse createBillingHistoryResponse(List<Trip> trips, List<Bill> bills, User user) {
        // Create a map of bills by trip ID for easy lookup
        Map<Long, Bill> billByTripId = bills.stream()
                .collect(Collectors.toMap(
                        bill -> bill.getTripId().value(),
                        bill -> bill
                ));

        // Build the list of TripBillDTO objects
        List<UserBillingHistoryResponse.TripBillDTO> tripBills = trips.stream()
                .filter(trip -> {
                    // Exclude trips that are incomplete (no bill or bill status is null, and trip not finished)
                    Bill bill = billByTripId.get(trip.getTripId().value());
                    boolean hasBillWithStatus = bill != null && bill.getStatus() != null;
                    boolean isTripComplete = trip.getEndTime() != null;
                    return hasBillWithStatus || isTripComplete;
                })
                .map(trip -> {
                    Bill bill = billByTripId.get(trip.getTripId().value());

                    // Get station names (handle null for trips in progress)
                    Station startStation = stationRepository.findById(trip.getStartStationId()).orElse(null);
                    Station endStation = stationRepository.findById(trip.getEndStationId()).orElse(null);

                    String startStationName = startStation != null ? startStation.getStationName() : "Unknown";
                    String endStationName = endStation != null ? endStation.getStationName() : "In Progress";

                    // Calculate regular cost (without discount)
                    double regularCost = 0.0;
                    if (trip.getPricingStrategy() != null) {
                        regularCost = trip.getPricingStrategy().calculateCost(trip.calculateDurationInMinutes());
                    }

                    return new UserBillingHistoryResponse.TripBillDTO(
                            trip.getTripId().value(),
                            trip.getBikeId().value(),
                            startStationName,
                            endStationName,
                            trip.getStartTime(),
                            trip.getEndTime(),
                            Math.round(trip.calculateDurationInMinutes()),
                            bill != null ? bill.getBillId().value() : null,
                            bill != null && bill.getStatus() != null ? bill.getStatus().name() : "PENDING",
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPricingTypeName() : "Standard Bike Pricing",
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getBaseFee() : 0.0,
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPerMinuteRate() : 0.0,
                            bill != null ? bill.getDiscountedCost() : 0.0,
                            regularCost
                    );
                })
                .collect(Collectors.toList());

        // Calculate total amount spent
        Double totalAmountSpent = bills.stream()
                .mapToDouble(Bill::getDiscountedCost)
                .sum();

        // Calculate outstanding bills (PENDING status only, PAID bills are already settled)
        List<Bill> outstandingBills = bills.stream()
                .filter(bill -> bill.getStatus() != null && "PENDING".equals(bill.getStatus().name()))
                .toList();

        Double totalOutstandingAmount = outstandingBills.stream()
                .mapToDouble(Bill::getDiscountedCost)
                .sum();

        Integer totalOutstandingBills = outstandingBills.size();

        // Build and return the response
        return new UserBillingHistoryResponse(
                user.getEmail(),
                user.getFullName(),
                totalAmountSpent,
                trips.size(),
                totalOutstandingAmount,
                totalOutstandingBills,
                tripBills
        );
    }

    private AllBillingHistoryResponse createAllSystemBillingHistoryResponse(List<Trip> trips, List<Bill> bills) {
        // Create a map of bills by trip ID for easy lookup
        Map<Long, Bill> billByTripId = bills.stream()
                .collect(Collectors.toMap(
                        bill -> bill.getTripId().value(),
                        bill -> bill
                ));

        // Build the list of SystemTripBillDTO objects with user information
        List<AllBillingHistoryResponse.SystemTripBillDTO> systemTripBills = trips.stream()
                .filter(trip -> {
                    // Exclude trips that are incomplete (no bill or bill status is null, and trip not finished)
                    Bill bill = billByTripId.get(trip.getTripId().value());
                    boolean hasBillWithStatus = bill != null && bill.getStatus() != null;
                    boolean isTripComplete = trip.getEndTime() != null;
                    return hasBillWithStatus || isTripComplete;
                })
                .map(trip -> {
                    Bill bill = billByTripId.get(trip.getTripId().value());

                    // Get user information
                    User user = userRepository.findById(trip.getUserId())
                            .orElse(null);
                    String userEmail = user != null ? user.getEmail() : "Unknown";
                    String userFullName = user != null ? user.getFullName() : "Unknown";

                    // Get station names (handle null for trips in progress)
                    Station startStation = stationRepository.findById(trip.getStartStationId()).orElse(null);
                    Station endStation = stationRepository.findById(trip.getEndStationId()).orElse(null);

                    String startStationName = startStation != null ? startStation.getStationName() : "Unknown";
                    String endStationName = endStation != null ? endStation.getStationName() : "In Progress";

                    // Calculate regular cost (without discount)
                    double regularCost = 0.0;
                    if (trip.getPricingStrategy() != null) {
                        regularCost = trip.getPricingStrategy().calculateCost(trip.calculateDurationInMinutes());
                    }

                    return new AllBillingHistoryResponse.SystemTripBillDTO(
                            trip.getTripId().value(),
                            trip.getUserId().value(),
                            userEmail,
                            userFullName,
                            trip.getBikeId().value(),
                            startStationName,
                            endStationName,
                            trip.getStartTime(),
                            trip.getEndTime(),
                            Math.round(trip.calculateDurationInMinutes()),
                            bill != null ? bill.getBillId().value() : null,
                            bill != null && bill.getStatus() != null ? bill.getStatus().name() : "PENDING",
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPricingTypeName() : "Standard Bike Pricing",
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getBaseFee() : 0.0,
                            trip.getPricingStrategy() != null ? trip.getPricingStrategy().getPerMinuteRate() : 0.0,
                            bill != null ? bill.getDiscountedCost() : 0.0,
                            regularCost
                    );
                })
                .collect(Collectors.toList());

        // Calculate total system revenue (all bills)
        Double totalSystemRevenue = bills.stream()
                .mapToDouble(Bill::getDiscountedCost)
                .sum();

        // Calculate outstanding bills (PENDING status only)
        List<Bill> outstandingBills = bills.stream()
                .filter(bill -> bill.getStatus() != null && "PENDING".equals(bill.getStatus().name()))
                .toList();

        Double totalSystemOutstandingAmount = outstandingBills.stream()
                .mapToDouble(Bill::getDiscountedCost)
                .sum();

        Integer totalSystemOutstandingBills = outstandingBills.size();

        // Build and return the response
        return new AllBillingHistoryResponse(
                totalSystemRevenue,
                trips.size(),
                totalSystemOutstandingAmount,
                totalSystemOutstandingBills,
                systemTripBills
        );
    }

}
