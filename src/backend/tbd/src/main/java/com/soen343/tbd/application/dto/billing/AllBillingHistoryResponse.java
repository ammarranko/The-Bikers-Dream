package com.soen343.tbd.application.dto.billing;

import java.sql.Timestamp;
import java.util.List;

/**
 * DTO for returning all billing history across all users in the system.
 */
public class AllBillingHistoryResponse {
    private Double totalSystemRevenue;
    private Integer totalSystemTrips;
    private Double totalSystemOutstandingAmount;
    private Integer totalSystemOutstandingBills;
    private List<SystemTripBillDTO> allTripBills;

    public AllBillingHistoryResponse(Double totalSystemRevenue, Integer totalSystemTrips,
                                     Double totalSystemOutstandingAmount, Integer totalSystemOutstandingBills,
                                     List<SystemTripBillDTO> allTripBills) {
        this.totalSystemRevenue = totalSystemRevenue;
        this.totalSystemTrips = totalSystemTrips;
        this.totalSystemOutstandingAmount = totalSystemOutstandingAmount;
        this.totalSystemOutstandingBills = totalSystemOutstandingBills;
        this.allTripBills = allTripBills;
    }

    public Double getTotalSystemRevenue() {
        return totalSystemRevenue;
    }

    public void setTotalSystemRevenue(Double totalSystemRevenue) {
        this.totalSystemRevenue = totalSystemRevenue;
    }

    public Integer getTotalSystemTrips() {
        return totalSystemTrips;
    }

    public void setTotalSystemTrips(Integer totalSystemTrips) {
        this.totalSystemTrips = totalSystemTrips;
    }

    public Double getTotalSystemOutstandingAmount() {
        return totalSystemOutstandingAmount;
    }

    public void setTotalSystemOutstandingAmount(Double totalSystemOutstandingAmount) {
        this.totalSystemOutstandingAmount = totalSystemOutstandingAmount;
    }

    public Integer getTotalSystemOutstandingBills() {
        return totalSystemOutstandingBills;
    }

    public void setTotalSystemOutstandingBills(Integer totalSystemOutstandingBills) {
        this.totalSystemOutstandingBills = totalSystemOutstandingBills;
    }

    public List<SystemTripBillDTO> getAllTripBills() {
        return allTripBills;
    }

    public void setAllTripBills(List<SystemTripBillDTO> allTripBills) {
        this.allTripBills = allTripBills;
    }

    /**
     * Inner DTO representing a single trip with its associated bill information across all users.
     */
    public static class SystemTripBillDTO {
        private Long tripId;
        private Long userId;
        private String userEmail;
        private String userFullName;
        private Long bikeId;
        private String startStationName;
        private String endStationName;
        private Timestamp startTime;
        private Timestamp endTime;
        private Long durationMinutes;

        // Bill information
        private Long billId;
        private String billStatus;
        private String pricingStrategy;
        private Double baseFare;
        private Double perMinuteRate;
        private Double totalAmount;
        private Double regularCost; // Added field

        public SystemTripBillDTO(Long tripId, Long userId, String userEmail, String userFullName,
                                 Long bikeId, String startStationName, String endStationName,
                                 Timestamp startTime, Timestamp endTime, Long durationMinutes,
                                 Long billId, String billStatus, String pricingStrategy,
                                 Double baseFare, Double perMinuteRate, Double totalAmount, Double regularCost) { // Added parameter
            this.tripId = tripId;
            this.userId = userId;
            this.userEmail = userEmail;
            this.userFullName = userFullName;
            this.bikeId = bikeId;
            this.startStationName = startStationName;
            this.endStationName = endStationName;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
            this.billId = billId;
            this.billStatus = billStatus;
            this.pricingStrategy = pricingStrategy;
            this.baseFare = baseFare;
            this.perMinuteRate = perMinuteRate;
            this.totalAmount = totalAmount;
            this.regularCost = regularCost; // Added assignment
        }

        public Long getTripId() {
            return tripId;
        }

        public void setTripId(Long tripId) {
            this.tripId = tripId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getUserFullName() {
            return userFullName;
        }

        public void setUserFullName(String userFullName) {
            this.userFullName = userFullName;
        }

        public Long getBikeId() {
            return bikeId;
        }

        public void setBikeId(Long bikeId) {
            this.bikeId = bikeId;
        }

        public String getStartStationName() {
            return startStationName;
        }

        public void setStartStationName(String startStationName) {
            this.startStationName = startStationName;
        }

        public String getEndStationName() {
            return endStationName;
        }

        public void setEndStationName(String endStationName) {
            this.endStationName = endStationName;
        }

        public Timestamp getStartTime() {
            return startTime;
        }

        public void setStartTime(Timestamp startTime) {
            this.startTime = startTime;
        }

        public Timestamp getEndTime() {
            return endTime;
        }

        public void setEndTime(Timestamp endTime) {
            this.endTime = endTime;
        }

        public Long getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Long durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public Long getBillId() {
            return billId;
        }

        public void setBillId(Long billId) {
            this.billId = billId;
        }

        public String getBillStatus() {
            return billStatus;
        }

        public void setBillStatus(String billStatus) {
            this.billStatus = billStatus;
        }

        public String getPricingStrategy() {
            return pricingStrategy;
        }

        public void setPricingStrategy(String pricingStrategy) {
            this.pricingStrategy = pricingStrategy;
        }

        public Double getBaseFare() {
            return baseFare;
        }

        public void setBaseFare(Double baseFare) {
            this.baseFare = baseFare;
        }

        public Double getPerMinuteRate() {
            return perMinuteRate;
        }

        public void setPerMinuteRate(Double perMinuteRate) {
            this.perMinuteRate = perMinuteRate;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public Double getRegularCost() {
            return regularCost;
        }

        public void setRegularCost(Double regularCost) {
            this.regularCost = regularCost;
        }
    }
}
