package com.soen343.tbd.application.dto.billing;

import java.sql.Timestamp;
import java.util.List;

/**
 * DTO for returning a user's complete billing history.
 */
public class UserBillingHistoryResponse {
    private String userEmail;
    private String fullName;
    private Double totalAmountSpent;
    private Integer totalTrips;
    private Double totalOutstandingAmount;
    private Integer totalOutstandingBills;
    private List<TripBillDTO> tripBills;

    public UserBillingHistoryResponse(String userEmail, String fullName, Double totalAmountSpent,
                                  Integer totalTrips, Double totalOutstandingAmount,
                                  Integer totalOutstandingBills, List<TripBillDTO> tripBills) {
        this.userEmail = userEmail;
        this.fullName = fullName;
        this.totalAmountSpent = totalAmountSpent;
        this.totalTrips = totalTrips;
        this.totalOutstandingAmount = totalOutstandingAmount;
        this.totalOutstandingBills = totalOutstandingBills;
        this.tripBills = tripBills;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Double getTotalAmountSpent() {
        return totalAmountSpent;
    }

    public void setTotalAmountSpent(Double totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public Integer getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(Integer totalTrips) {
        this.totalTrips = totalTrips;
    }

    public Double getTotalOutstandingAmount() {
        return totalOutstandingAmount;
    }

    public void setTotalOutstandingAmount(Double totalOutstandingAmount) {
        this.totalOutstandingAmount = totalOutstandingAmount;
    }

    public Integer getTotalOutstandingBills() {
        return totalOutstandingBills;
    }

    public void setTotalOutstandingBills(Integer totalOutstandingBills) {
        this.totalOutstandingBills = totalOutstandingBills;
    }

    public List<TripBillDTO> getTripBills() {
        return tripBills;
    }

    public void setTripBills(List<TripBillDTO> tripBills) {
        this.tripBills = tripBills;
    }

    /**
     * Inner DTO representing a single trip with its associated bill information.
     */
    public static class TripBillDTO {
        private Long tripId;
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

        public TripBillDTO(Long tripId, Long bikeId, String startStationName, String endStationName,
                          Timestamp startTime, Timestamp endTime, Long durationMinutes,
                          Long billId, String billStatus, String pricingStrategy,
                          Double baseFare, Double perMinuteRate, Double totalAmount) {
            this.tripId = tripId;
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
        }

        public Long getTripId() {
            return tripId;
        }

        public void setTripId(Long tripId) {
            this.tripId = tripId;
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
    }
}

