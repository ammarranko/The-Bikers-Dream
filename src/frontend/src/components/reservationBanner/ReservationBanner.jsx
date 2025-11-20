import React from 'react';
import './ReservationBanner.css';

const ReservationBanner = ({ activeReservation, timeLeft }) => {
    if (!activeReservation?.hasActiveReservation) return null;

    return (
        <div className="reservation-banner">
            <div className="reservation-header">
                <i className="fas fa-clock reservation-icon"></i>
                <h3>Active Reservation</h3>
            </div>
            
            <div className="reservation-details">
                <div className="detail-row">
                    <span className="detail-label">Station:</span>
                    <span className="detail-value">#{activeReservation.stationId}</span>
                </div>
                <div className="detail-row">
                    <span className="detail-label">Bike:</span>
                    <span className="detail-value">#{activeReservation.bikeId}</span>
                </div>
                <div className="detail-row time-remaining">
                    <span className="detail-label">Time Remaining:</span>
                    <span className="detail-value countdown">{timeLeft}
                    <span className="extra-time">
                    {(() => {
            const userTier = localStorage.getItem('tier'); // Get user tier
            if (userTier === 'SILVER') return ' +2 min for Silver';
            if (userTier === 'GOLD') return ' +5 min for Gold';
            return '';
        })()} </span>
        </span>
                </div>
            </div>

            <p className="reservation-notice">
                Please pick up your bike within the remaining time to avoid cancellation
            </p>
        </div>
    );
};

export default ReservationBanner;
