import React from 'react';
import './TripSummaryPopup.css';

const TripSummaryPopup = ({ tripSummary, onClose }) => {
    if (!tripSummary) return null;

    return (
        <div className="trip-summary-overlay" onClick={onClose}>
            <div className="trip-summary-popup" onClick={(e) => e.stopPropagation()}>
                <div className="trip-summary-header">
                     Bike Return Successful!
                </div>

                <div className="trip-summary-section">
                    <div className="trip-summary-section-title">
                        TRIP SUMMARY
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Trip ID</span>
                        <span className="trip-summary-value">#{tripSummary.tripId}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Bike ID</span>
                        <span className="trip-summary-value">#{tripSummary.bikeId}</span>
                    </div>

                    <div className="divider"></div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Start</span>
                        <span className="trip-summary-value">{tripSummary.startStationName}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">End</span>
                        <span className="trip-summary-value">{tripSummary.endStationName}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Duration</span>
                        <span className="trip-summary-value">{tripSummary.durationMinutes} minutes</span>
                    </div>
                </div>

                <div className="trip-summary-section">
                    <div className="trip-summary-section-title">
                        BILLING DETAILS
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Bill ID</span>
                        <span className="trip-summary-value">#{tripSummary.billId}</span>
                    </div>

                    <div className="divider"></div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Base Fare</span>
                        <span className="trip-summary-value">${tripSummary.baseFare.toFixed(2)}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Rate</span>
                        <span className="trip-summary-value">${tripSummary.perMinuteRate.toFixed(2)}/min</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Duration Charge</span>
                        <span className="trip-summary-value">
                            ${(tripSummary.perMinuteRate * tripSummary.durationMinutes).toFixed(2)}
                        </span>
                    </div>

                    <div className="trip-summary-total">
                        <span>TOTAL</span>
                        <span>${tripSummary.totalAmount.toFixed(2)}</span>
                    </div>
                </div>

                <div className="trip-summary-footer">
                    Thank you for riding with TheBiker's Dream!
                </div>

                <button className="trip-summary-close-btn" onClick={onClose}>
                    Close
                </button>
            </div>
        </div>
    );
};

export default TripSummaryPopup;

