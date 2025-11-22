package com.soen343.tbd.application.controller;

import com.soen343.tbd.application.service.LoyaltyTierService;
import com.soen343.tbd.application.service.TestDatabaseService;
import com.soen343.tbd.domain.model.Bike;
import com.soen343.tbd.domain.model.Dock;
import com.soen343.tbd.domain.model.Station;
import com.soen343.tbd.domain.model.Trip;
import com.soen343.tbd.domain.model.enums.TierType;
import com.soen343.tbd.domain.model.enums.TripStatus;
import com.soen343.tbd.domain.model.ids.BikeId;
import com.soen343.tbd.domain.model.ids.StationId;
import com.soen343.tbd.domain.model.user.User;
import com.soen343.tbd.domain.repository.ReservationRepository;
import com.soen343.tbd.domain.repository.TripRepository;
import com.soen343.tbd.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/test")
public class TestDatabaseController {
    private final TestDatabaseService testDatabaseService;

    @Autowired
    private LoyaltyTierService loyaltyTierService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseController.class);

    public TestDatabaseController(TestDatabaseService testDatabaseService) {
        this.testDatabaseService = testDatabaseService;
    }

    @GetMapping("/bikes/{id}")
    public ResponseEntity<Bike> getBike(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Bike with ID: {}", id);
        Bike bike = testDatabaseService.getBikeById(id);
        if (bike != null) {
            logger.info("Successfully retrieved bike for id: {}", id);
            return ResponseEntity.ok(bike);
        }
        logger.warn("Bike not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/docks/{id}")
    public ResponseEntity<Dock> getDock(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Dock with ID: {}", id);
        Dock dock = testDatabaseService.getDockById(id);
        if (dock != null) {
            logger.info("Successfully retrieved dock for id: {}", id);
            return ResponseEntity.ok(dock);
        }
        logger.warn("Dock not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<Station> getStation(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get Station with ID: {}", id);
        Station station = testDatabaseService.getStationById(id);
        if (station != null) {
            logger.info("Successfully retrieved station for id: {}", id);
            return ResponseEntity.ok(station);
        }
        logger.warn("Station not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        logger.info("REQUEST RECEIVED | Get User with ID: {}", id);
        User user = testDatabaseService.getUserById(id);
        if (user != null) {
            logger.info("Successfully retrieved user for id: {}", id);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for id: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        logger.info("REQUEST RECEIVED | Get User with email: {}", email);
        User user = testDatabaseService.getUserByEmail(email);
        if (user != null) {
            logger.info("Successfully retrieved user for email: {}", email);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for email: {}", email);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        logger.info("REQUEST RECEIVED | Get User with username: {}", username);
        User user = testDatabaseService.getUserByUsername(username);
        if (user != null) {
            logger.info("Successfully retrieved user for username: {}", username);
            return ResponseEntity.ok(user);
        }
        logger.warn("User not found for username: {}", username);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/update-all-tiers")
    public ResponseEntity<Map<String, Object>> updateAllUserTiers() {
        logger.info("REQUEST RECEIVED | Update all user tiers");

        try {
            List<User> allUsers = userRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            Map<String, Integer> tierCounts = new HashMap<>();
            tierCounts.put("NONE", 0);
            tierCounts.put("BRONZE", 0);
            tierCounts.put("SILVER", 0);
            tierCounts.put("GOLD", 0);

            for (User user : allUsers) {
                String oldTier = user.getTierType() != null ? user.getTierType().name() : "NONE";
                loyaltyTierService.updateUserTier(user);
                String newTier = user.getTierType() != null ? user.getTierType().name() : "NONE";

                tierCounts.put(newTier, tierCounts.get(newTier) + 1);

                logger.info("User {} ({}) - Old Tier: {}, New Tier: {}",
                    user.getEmail(), user.getUsername(), oldTier, newTier);
            }

            response.put("success", true);
            response.put("totalUsers", allUsers.size());
            response.put("tierDistribution", tierCounts);
            response.put("message", "All user tiers have been updated successfully");

            logger.info("Successfully updated tiers for {} users", allUsers.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating user tiers: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating tiers: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/debug-tier/{email}")
    public ResponseEntity<Map<String, Object>> debugUserTier(@PathVariable String email) {
        logger.info("REQUEST RECEIVED | Debug tier for user: {}", email);

        try {
            User user = testDatabaseService.getUserByEmail(email);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneYearAgo = now.minusYears(1);

            // Bronze tier checks
            int missedReservations = reservationRepository.missedReservationscount(user.getUserId(), oneYearAgo);
            int unreturnedBikes = tripRepository.countUnreturnedBikesByUser(user.getUserId());
            int tripsLastYear = tripRepository.countTripsForUserByIdSince(user.getUserId(), oneYearAgo);

            response.put("userEmail", email);
            response.put("currentTier", user.getTierType().name());
            response.put("bronzeChecks", Map.of(
                "missedReservations", missedReservations,
                "unreturnedBikes", unreturnedBikes,
                "tripsLastYear", tripsLastYear,
                "qualifiesForBronze", missedReservations == 0 && unreturnedBikes == 0 && tripsLastYear >= 10
            ));

            // Silver tier checks - last 3 months (30-day periods from today)
            Map<String, Object> monthlyTripsDetails = new HashMap<>();
            boolean allPeriodsHave5Plus = true;

            for (int i = 0; i < 3; i++) {
                LocalDateTime periodEnd = now.minusDays(i * 30L);
                LocalDateTime periodStart = periodEnd.minusDays(30);
                int tripsThisPeriod = tripRepository.countTripsForUserBetween(user.getUserId(), periodStart, periodEnd);

                String periodName = "Period " + (i + 1) + " (" + periodStart.toLocalDate() + " to " + periodEnd.toLocalDate() + ")";
                monthlyTripsDetails.put(periodName, tripsThisPeriod);

                if (tripsThisPeriod < 5) {
                    allPeriodsHave5Plus = false;
                }
            }

            response.put("silverChecks", Map.of(
                "periodsChecked", monthlyTripsDetails,
                "allPeriodsHave5Plus", allPeriodsHave5Plus,
                "note", "Checking 30-day periods: today back to 90 days ago"
            ));

            // Calculate what tier they should be
            TierType calculatedTier = loyaltyTierService.calculateUserTier(user);
            response.put("calculatedTier", calculatedTier.name());
            response.put("needsUpdate", !user.getTierType().equals(calculatedTier));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error debugging user tier: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/generate-gold-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateGoldTierTrips(@PathVariable String email) {
        return generateTripsForUser(email, "GOLD");
    }

    @GetMapping("/generate-gold-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateGoldTierTripsGet(@PathVariable String email) {
        return generateTripsForUser(email, "GOLD");
    }

    @PostMapping("/generate-silver-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateSilverTierTrips(@PathVariable String email) {
        return generateTripsForUser(email, "SILVER");
    }

    @GetMapping("/generate-silver-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateSilverTierTripsGet(@PathVariable String email) {
        return generateTripsForUser(email, "SILVER");
    }

    @PostMapping("/generate-bronze-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateBronzeTierTrips(@PathVariable String email) {
        return generateTripsForUser(email, "BRONZE");
    }

    @GetMapping("/generate-bronze-trips/{email}")
    public ResponseEntity<Map<String, Object>> generateBronzeTierTripsGet(@PathVariable String email) {
        return generateTripsForUser(email, "BRONZE");
    }

    private ResponseEntity<Map<String, Object>> generateTripsForUser(String email, String targetTier) {
        logger.info("REQUEST RECEIVED | Generate {} tier trips for user: {}", targetTier, email);

        try {
            User user = testDatabaseService.getUserByEmail(email);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            // Get all existing trips to find valid bike and station IDs
            List<Trip> existingTrips = tripRepository.findAll();
            if (existingTrips.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No existing trips found. Cannot determine valid bike/station IDs. Please complete at least one real trip first.");
                return ResponseEntity.status(400).body(errorResponse);
            }

            // Extract valid IDs from existing trips
            Trip sampleTrip = existingTrips.get(0);
            BikeId validBikeId = sampleTrip.getBikeId();
            StationId validStartStationId = sampleTrip.getStartStationId();
            StationId validEndStationId = sampleTrip.getEndStationId() != null ? sampleTrip.getEndStationId() : validStartStationId;

            Random random = new Random();
            LocalDateTime now = LocalDateTime.now();
            int totalTripsCreated = 0;
            int expectedTrips = 0;
            String tierDescription = "";

            // Different generation logic based on target tier
            switch (targetTier) {
                case "BRONZE":
                    // Bronze: 9 trips in the last year (10th trip to be added manually)
                    expectedTrips = 9;
                    tierDescription = "9 trips over the last year (add 1 more manually to qualify for Bronze)";
                    LocalDateTime bronzeYearAgo = now.minusYears(1);

                    for (int i = 0; i < 9; i++) {
                        // Random time within the last year (excluding today)
                        long daysInYear = 365;
                        long randomDays = random.nextInt((int) daysInYear - 1) + 1; // 1-364 days ago, excluding today
                        long randomHours = random.nextInt(24);
                        long randomMinutes = random.nextInt(60);

                        LocalDateTime tripStart = bronzeYearAgo.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes);
                        int durationMinutes = 15 + random.nextInt(45);
                        LocalDateTime tripEnd = tripStart.plusMinutes(durationMinutes);

                        createAndSaveTrip(user, validBikeId, validStartStationId, validEndStationId, tripStart, tripEnd);
                        totalTripsCreated++;
                    }
                    break;

                case "SILVER":
                    // Silver: 8 trips (5 for month 2, 5 for month 3, 4 for current month) - 1 trip to be added manually for current month
                    expectedTrips = 14;
                    tierDescription = "5 trips for 2 oldest months, 4 trips for current month (add 1 more trip for current month manually)";

                    // Generate trips for all 3 periods
                    for (int period = 0; period < 3; period++) {
                        LocalDateTime periodEnd = now.minusDays(period * 30L);
                        LocalDateTime periodStart = periodEnd.minusDays(30);

                        // For the current month (period 0), only generate 4 trips
                        int tripsForThisPeriod = (period == 0) ? 4 : 5;

                        for (int tripInPeriod = 0; tripInPeriod < tripsForThisPeriod; tripInPeriod++) {
                            // Random time within this 30-day period
                            long randomDays = random.nextInt(30);
                            long randomHours = random.nextInt(24);
                            long randomMinutes = random.nextInt(60);

                            LocalDateTime tripStart = periodStart.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes);
                            int durationMinutes = 15 + random.nextInt(45);
                            LocalDateTime tripEnd = tripStart.plusMinutes(durationMinutes);

                            createAndSaveTrip(user, validBikeId, validStartStationId, validEndStationId, tripStart, tripEnd);
                            totalTripsCreated++;
                        }
                    }
                    break;

                case "GOLD":
                    // Gold: 59 trips (5 per week for 11 weeks, 4 for current week) - 1 trip to be added manually for current week
                    expectedTrips = 59;
                    tierDescription = "5 trips per week for 11 weeks, 4 trips for current week (add 1 more trip for current week manually)";

                    // Generate trips for all 12 weeks
                    for (int week = 0; week < 12; week++) {
                        LocalDateTime weekStart = now.minusWeeks(week + 1);

                        // For the current week (week 0), only generate 4 trips
                        int tripsForThisWeek = (week == 0) ? 4 : 5;

                        for (int tripInWeek = 0; tripInWeek < tripsForThisWeek; tripInWeek++) {
                            // Random time within this week
                            long randomDays = random.nextInt(7);
                            long randomHours = random.nextInt(24);
                            long randomMinutes = random.nextInt(60);

                            LocalDateTime tripStart = weekStart.plusDays(randomDays).plusHours(randomHours).plusMinutes(randomMinutes);
                            int durationMinutes = 15 + random.nextInt(45);
                            LocalDateTime tripEnd = tripStart.plusMinutes(durationMinutes);

                            createAndSaveTrip(user, validBikeId, validStartStationId, validEndStationId, tripStart, tripEnd);
                            totalTripsCreated++;
                        }
                    }
                    break;

                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Invalid tier: " + targetTier);
                    return ResponseEntity.status(400).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userEmail", email);
            response.put("targetTier", targetTier);
            response.put("tierDescription", tierDescription);
            response.put("tripsCreated", totalTripsCreated);
            response.put("expectedTrips", expectedTrips);
            response.put("currentTier", user.getTierType().name());
            response.put("message", "Successfully generated " + totalTripsCreated + " trips for " + targetTier + " tier. Use /api/test/update-all-tiers to update tier.");

            logger.info("Generated {} trips for user {}", totalTripsCreated, email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error generating trips: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private void createAndSaveTrip(User user, BikeId bikeId, StationId startStationId, StationId endStationId,
                                    LocalDateTime tripStart, LocalDateTime tripEnd) {
        // Convert to Timestamp
        Timestamp startTime = Timestamp.from(tripStart.atZone(ZoneId.systemDefault()).toInstant());
        Timestamp endTime = Timestamp.from(tripEnd.atZone(ZoneId.systemDefault()).toInstant());

        // Create a completed trip
        Trip trip = new Trip();
        trip.setTripId(null); // Auto-generated
        trip.setUserId(user.getUserId());
        trip.setBikeId(bikeId);
        trip.setStartStationId(startStationId);
        trip.setEndStationId(endStationId);
        trip.setStartTime(startTime);
        trip.setEndTime(endTime);
        trip.setStatus(TripStatus.COMPLETED);

        tripRepository.save(trip);
    }
}

