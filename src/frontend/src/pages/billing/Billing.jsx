import React, { useState, useEffect } from 'react';
import '../home/Home.css';  // Use Home.css instead of Billing.css
import './Billing.css';  // Additional billing-specific styles
import NavigationBar from '../../components/navigationBar/NavigationBar';
import LoadingSpinner from '../../components/loadingSpinner/LoadingSpinner';
import { useNavigate } from 'react-router-dom';

function Billing() {
    const [billingData, setBillingData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedBill, setSelectedBill] = useState(null);
    const [paymentProcessing, setPaymentProcessing] = useState(false);
    const navigate = useNavigate();

    // Use the same localStorage keys as Home page
    const fullName = localStorage.getItem('user_full_name');
    const role = localStorage.getItem('user_role');

    useEffect(() => {
        fetchBillingHistory();
    }, []);

    const fetchBillingHistory = async () => {
        try {
            setIsLoading(true);
            const token = localStorage.getItem('jwt_token');

            const response = await fetch('http://localhost:8080/api/billing/user/history', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch billing history');
            }

            const data = await response.json();
            setBillingData(data);
            setError(null);
        } catch (err) {
            console.error('Error fetching billing history:', err);
            setError(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    const handleBillingClick = () => {
        // Already on billing page, refresh data
        fetchBillingHistory();
    };

    const formatDate = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const handlePayNow = (tripBill) => {
        setSelectedBill(tripBill);
    };

    const handlePaymentSubmit = async (e) => {
        e.preventDefault();
        setPaymentProcessing(true);

        try {
            const token = localStorage.getItem('jwt_token');

            const response = await fetch(`http://localhost:8080/api/billing/trip/${selectedBill.tripId}/pay`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Payment failed');
            }

            // Refresh billing data after successful payment
            await fetchBillingHistory();
            setSelectedBill(null);
            alert('Payment successful!');
        } catch (err) {
            console.error('Payment error:', err);
            alert('Payment failed. Please try again.');
        } finally {
            setPaymentProcessing(false);
        }
    };

    const handleCancelPayment = () => {
        setSelectedBill(null);
    };

    return (
        <div className="home-container">
            {isLoading && <LoadingSpinner message="Loading your billing history..." />}

            <NavigationBar
                fullName={fullName}
                role={role}
                handleLogout={handleLogout}
                handleBillingClick={handleBillingClick}
            />

            <div className="content-wrapper">
                <div className="welcome-section">
                    <h1 className="welcome-title">
                        {fullName ? (
                            `${fullName.split(' ')[0]}'s Billing History`
                        ) : (
                            'Billing History'
                        )}
                    </h1>
                    <p className="welcome-subtitle">
                        {billingData ? (
                            `${billingData.totalTrips} trips • $${billingData.totalAmountSpent.toFixed(2)} total spent`
                        ) : (
                            'View your trip history and billing details'
                        )}
                    </p>
                </div>

                <div className="dashboard-grid">
                    {/* Replace map-container with billing cards */}
                    <div className="map-container">
                        <h2 className="map-title">
                            Bill History
                        </h2>

                        <div className="billing-content">
                            {error && (
                                <div className="error-message">
                                    <i className="fas fa-exclamation-circle"></i>
                                    <p>Error: {error}</p>
                                    <button onClick={fetchBillingHistory} className="retry-btn">
                                        Retry
                                    </button>
                                </div>
                            )}

                            {!isLoading && !error && billingData && billingData.tripBills.length === 0 && (
                                <div className="empty-message">
                                    <i className="fas fa-inbox"></i>
                                    <p>No trips yet. Start riding to see your billing history!</p>
                                </div>
                            )}

                            {!isLoading && !error && billingData && billingData.tripBills.length > 0 && (
                                <div className="trip-bills-list">
                                    {billingData.tripBills.map((tripBill) => (
                                        <div key={tripBill.tripId} className="trip-bill-card">
                                            {/* Card Header */}
                                            <div className="card-header">
                                                <div className="trip-id">
                                                    <i className="fas fa-route"></i> Bill #{tripBill.billId}
                                                </div>
                                                <span className={`status-badge ${tripBill.billStatus.toLowerCase()}`}>
                                                    {tripBill.billStatus}
                                                </span>
                                            </div>

                                            {/* Route Information */}
                                            <div className="route-section">
                                                <div className="route-point start">
                                                    <i className="fas fa-map-marker-alt"></i>
                                                    <span>{tripBill.startStationName}</span>
                                                </div>
                                                <div className="route-arrow">
                                                    <i className="fas fa-arrow-down"></i>
                                                </div>
                                                <div className="route-point end">
                                                    <i className="fas fa-flag-checkered"></i>
                                                    <span>{tripBill.endStationName}</span>
                                                </div>
                                            </div>

                                            {/* Trip Details */}
                                            <div className="trip-details">
                                                <div className="detail-row">
                                                    <span className="detail-label">
                                                        <i className="fas fa-calendar"></i> Date
                                                    </span>
                                                    <span className="detail-value">
                                                        {formatDate(tripBill.startTime)}
                                                    </span>
                                                </div>
                                                <div className="detail-row">
                                                    <span className="detail-label">
                                                        <i className="fas fa-clock"></i> Duration
                                                    </span>
                                                    <span className="detail-value">
                                                        {tripBill.durationMinutes} min
                                                    </span>
                                                </div>
                                                <div className="detail-row">
                                                    <span className="detail-label">
                                                        <i className="fas fa-bicycle"></i> Bike Id
                                                    </span>
                                                    <span className="detail-value">
                                                        #{tripBill.bikeId}
                                                    </span>
                                                </div>
                                            </div>

                                            {/* Pricing */}
                                            <div className="pricing-section">
                                                <div className="pricing-row">
                                                    <span>Base: ${tripBill.baseFare.toFixed(2)}</span>
                                                    <span>Rate: ${tripBill.perMinuteRate.toFixed(2)}/min</span>
                                                </div>
                                                <div className="pricing-total">
                                                    <span>Total</span>
                                                    <span>${tripBill.totalAmount.toFixed(2)}</span>
                                                </div>
                                            </div>

                                            {/* Pay Now Button */}
                                            {(tripBill.billStatus === 'PENDING') && (
                                                <button
                                                    className="pay-now-btn"
                                                    onClick={() => handlePayNow(tripBill)}
                                                >
                                                    <i className="fas fa-credit-card"></i> Pay Now
                                                </button>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Sidebar with Payment Component */}
                    <div className="sidebar-container">
                        {selectedBill ? (
                            <div className="payment-card">
                                <div className="payment-header">
                                    <h3>
                                        <i className="fas fa-credit-card"></i> Payment
                                    </h3>
                                    <button
                                        className="close-btn"
                                        onClick={handleCancelPayment}
                                        disabled={paymentProcessing}
                                    >
                                        <i className="fas fa-times"></i>
                                    </button>
                                </div>

                                <div className="payment-details">
                                    <h4>Trip Details</h4>
                                    <div className="payment-detail-item">
                                        <span>Trip #</span>
                                        <span>{selectedBill.tripId}</span>
                                    </div>
                                    <div className="payment-detail-item">
                                        <span>Route</span>
                                        <span className="route-text">
                                            {selectedBill.startStationName} → {selectedBill.endStationName}
                                        </span>
                                    </div>
                                    <div className="payment-detail-item">
                                        <span>Duration</span>
                                        <span>{selectedBill.durationMinutes} min</span>
                                    </div>
                                    <div className="payment-detail-item">
                                        <span>Date</span>
                                        <span>{formatDate(selectedBill.startTime)}</span>
                                    </div>
                                </div>

                                <div className="payment-breakdown">
                                    <h4>Payment Breakdown</h4>
                                    <div className="breakdown-item">
                                        <span>Base Fare</span>
                                        <span>${selectedBill.baseFare.toFixed(2)}</span>
                                    </div>
                                    <div className="breakdown-item">
                                        <span>Time Charge ({selectedBill.durationMinutes} min × ${selectedBill.perMinuteRate.toFixed(2)})</span>
                                        <span>${(selectedBill.durationMinutes * selectedBill.perMinuteRate).toFixed(2)}</span>
                                    </div>
                                    <div className="breakdown-total">
                                        <span>Total Amount</span>
                                        <span>${selectedBill.totalAmount.toFixed(2)}</span>
                                    </div>
                                </div>

                                <form onSubmit={handlePaymentSubmit} className="payment-form">
                                    <div className="form-group">
                                        <label>
                                            <i className="fas fa-credit-card"></i> Card Number
                                        </label>
                                        <input
                                            type="text"
                                            placeholder="1234 5678 9012 3456"
                                            required
                                            disabled={paymentProcessing}
                                            pattern="[0-9]{13,19}"
                                            maxLength="19"
                                        />
                                    </div>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>Expiry</label>
                                            <input
                                                type="text"
                                                placeholder="MM/YY"
                                                required
                                                disabled={paymentProcessing}
                                                pattern="[0-9]{2}/[0-9]{2}"
                                                maxLength="5"
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>CVV</label>
                                            <input
                                                type="text"
                                                placeholder="123"
                                                required
                                                disabled={paymentProcessing}
                                                pattern="[0-9]{3,4}"
                                                maxLength="4"
                                            />
                                        </div>
                                    </div>

                                    <div className="form-group">
                                        <label>
                                            <i className="fas fa-user"></i> Cardholder Name
                                        </label>
                                        <input
                                            type="text"
                                            placeholder="John Doe"
                                            required
                                            disabled={paymentProcessing}
                                        />
                                    </div>

                                    <div className="payment-actions">
                                        <button
                                            type="button"
                                            className="cancel-payment-btn"
                                            onClick={handleCancelPayment}
                                            disabled={paymentProcessing}
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            type="submit"
                                            className="submit-payment-btn"
                                            disabled={paymentProcessing}
                                        >
                                            {paymentProcessing ? (
                                                <>
                                                    <i className="fas fa-spinner fa-spin"></i> Processing...
                                                </>
                                            ) : (
                                                <>
                                                    <i className="fas fa-lock"></i> Pay ${selectedBill.totalAmount.toFixed(2)}
                                                </>
                                            )}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        ) : (
                            <div className="no-reservation-card">
                                <h3>
                                    <i className="fas fa-info-circle"></i> Payment
                                </h3>
                                <p className="helper-text">
                                    Select an unpaid bill to make a payment
                                </p>
                                {billingData && (
                                    <>
                                        <div className="summary-divider"></div>
                                        <p className="summary-stat">
                                            <strong>Total Trips:</strong> {billingData.totalTrips}
                                        </p>
                                        <p className="summary-stat">
                                            <strong>Total Spent:</strong> ${billingData.totalAmountSpent.toFixed(2)}
                                        </p>
                                    </>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Billing;

