import React, { useState, useEffect } from 'react';
import '../home/Home.css';  // Use Home.css instead of Billing.css
import './Billing.css';  // Additional billing-specific styles
import NavigationBar from '../../components/navigationBar/NavigationBar';
import LoadingSpinner from '../../components/loadingSpinner/LoadingSpinner';
import { useNavigate } from 'react-router-dom';

function Billing() {
    const [billingData, setBillingData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedBill, setSelectedBill] = useState(null);
    const [paymentProcessing, setPaymentProcessing] = useState(false);
    const [paymentForm, setPaymentForm] = useState({
        cardNumber: '',
        cardHolderName: '',
        expiryMonth: '',
        expiryYear: '',
        cvc: ''
    });
    const [sortBy, setSortBy] = useState('date-desc'); // date-desc, date-asc, price-desc, price-asc
    const [filterStatus, setFilterStatus] = useState('all'); // all, pending, paid
    const [searchUserId, setSearchUserId] = useState(''); // Search by User ID (operator only)
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage] = useState(5);
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

            // Use different endpoint based on user role
            const endpoint = role === 'OPERATOR'
                ? 'http://localhost:8080/api/billing/operator/allhistory'
                : 'http://localhost:8080/api/billing/user/history';

            const response = await fetch(endpoint, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                setBillingData(data);
            }
        } catch (err) {
            console.error('Error fetching billing history:', err);
        } finally {
            setIsLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    const handleHomeClick = () => {
        navigate('/home');
    };

    const handleBillingClick = () => {
        // Already on billing page, refresh data
        fetchBillingHistory();
    };

    const handleViewHistory = () => {
        navigate('/history');
    };

    const handlePricingClick = () => {
        navigate('/pricing');
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
        setPaymentForm({
            cardNumber: '',
            cardHolderName: '',
            expiryMonth: '',
            expiryYear: '',
            cvc: ''
        });
    };

    const handlePaymentSubmit = async (e) => {
        e.preventDefault();
        setPaymentProcessing(true);
        setIsLoading(true); // Show loading spinner during payment

        try {
            const token = localStorage.getItem('jwt_token');

            // Prepare the payment request matching UserPaymentRequest DTO
            const paymentRequest = {
                billId: selectedBill.billId,
                cardNumber: paymentForm.cardNumber,
                cardHolderName: paymentForm.cardHolderName,
                expiryMonth: paymentForm.expiryMonth,
                expiryYear: paymentForm.expiryYear,
                cvc: paymentForm.cvc
            };

            const response = await fetch('http://localhost:8080/api/billing/user/payment', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(paymentRequest)
            });

            if (!response.ok) {
                const errorData = await response.json();
                // Show specific error message about credentials
                alert('Payment Failed\n\nThe payment could not be processed. Please verify that your card information matches the details on file:\n\n• Card number\n• Cardholder name\n• Expiry date\n• CVV\n\nError: ' + (errorData.message || 'Invalid payment information'));
                setIsLoading(false); // Stop loading on error
                setPaymentProcessing(false);
                return;
            }

            await response.json(); // Consume response

            // Refresh billing data to update outstanding amounts and bill statuses
            await fetchBillingHistory();

            setSelectedBill(null);
            alert('Payment successful!');
        } catch (err) {
            console.error('Payment error:', err);
            alert('Payment Failed\n\nThe payment could not be processed. Please verify that your card information matches the details on file and try again.');
            setIsLoading(false); // Stop loading on error
        } finally {
            setPaymentProcessing(false);
            // Note: isLoading will be set to false by fetchBillingHistory() on success
        }
    };

    const handleCancelPayment = () => {
        setSelectedBill(null);
    };

    const handlePaymentFormChange = (field, value) => {
        setPaymentForm(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const getFilteredAndSortedBills = () => {
        if (!billingData) return [];

        // Handle different data structures for operator vs rider
        const bills = role === 'OPERATOR'
            ? (billingData.allTripBills || [])
            : (billingData.tripBills || []);

        let filteredBills = [...bills];

        // Apply status filter
        if (filterStatus === 'pending') {
            filteredBills = filteredBills.filter(bill => bill.billStatus === 'PENDING');
        } else if (filterStatus === 'paid') {
            filteredBills = filteredBills.filter(bill => bill.billStatus === 'PAID');
        }

        // Apply User ID search filter (only for operators)
        if (role === 'OPERATOR' && searchUserId.trim() !== '') {
            const searchValue = searchUserId.trim();
            filteredBills = filteredBills.filter(bill => {
                return bill.userId?.toString().includes(searchValue);
            });
        }

        // Apply sorting
        filteredBills.sort((a, b) => {
            switch (sortBy) {
                case 'date-desc':
                    return new Date(b.startTime) - new Date(a.startTime);
                case 'date-asc':
                    return new Date(a.startTime) - new Date(b.startTime);
                case 'price-desc':
                    return b.totalAmount - a.totalAmount;
                case 'price-asc':
                    return a.totalAmount - b.totalAmount;
                default:
                    return 0;
            }
        });

        return filteredBills;
    };

    // Pagination Logic
    const filteredBills = getFilteredAndSortedBills();
    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentBills = filteredBills.slice(indexOfFirstItem, indexOfLastItem);
    const totalPages = Math.ceil(filteredBills.length / itemsPerPage);

    const paginate = (pageNumber) => setCurrentPage(pageNumber);

    return (
        <div className="home-container">
            {isLoading && <LoadingSpinner message={paymentProcessing ? "Processing payment..." : "Loading your billing history..."} />}

            <NavigationBar
                fullName={fullName}
                role={role}
                handleLogout={handleLogout}
                handleBillingClick={handleBillingClick}
                handleHomeClick={handleHomeClick}
                handleViewHistory={handleViewHistory}
                handlePricingClick={handlePricingClick}
                activePage="billing"
            />

            <div className="content-wrapper">
                <div className="welcome-section">
                    <div>
                        <h1 className="welcome-title">
                            {role === 'OPERATOR' ? (
                                'System Billing History'
                            ) : fullName ? (
                                `${fullName.split(' ')[0]}'s Billing History`
                            ) : (
                                'Billing History'
                            )}
                        </h1>
                        <p className="welcome-subtitle">
                            {role === 'OPERATOR'
                                ? 'View all trips and billing details across the system'
                                : 'View your trip history and billing details'
                            }
                        </p>
                    </div>

                    {/* Filter Controls */}
                    <div className="filter-controls">
                        <div className="filter-group">
                            <label htmlFor="sort-select">
                                <i className="fas fa-sort"></i> Sort By:
                            </label>
                            <select
                                id="sort-select"
                                value={sortBy}
                                onChange={(e) => setSortBy(e.target.value)}
                                className="filter-select"
                            >
                                <option value="date-desc">Date (Newest First)</option>
                                <option value="date-asc">Date (Oldest First)</option>
                                <option value="price-desc">Price (High to Low)</option>
                                <option value="price-asc">Price (Low to High)</option>
                            </select>
                        </div>

                        <div className="filter-group">
                            <label htmlFor="status-select">
                                <i className="fas fa-filter"></i> Status:
                            </label>
                            <select
                                id="status-select"
                                value={filterStatus}
                                onChange={(e) => setFilterStatus(e.target.value)}
                                className="filter-select"
                            >
                                <option value="all">All Bills</option>
                                <option value="pending">Pending Only</option>
                                <option value="paid">Paid Only</option>
                            </select>
                        </div>

                        {/* User ID Search - Only visible for operators */}
                        {role === 'OPERATOR' && (
                            <div className="filter-group">
                                <label htmlFor="search-userid">
                                    <i className="fas fa-search"></i> Search User ID:
                                </label>
                                <input
                                    id="search-userid"
                                    type="text"
                                    placeholder="Enter User ID..."
                                    value={searchUserId}
                                    onChange={(e) => setSearchUserId(e.target.value)}
                                    className="filter-input"
                                />
                            </div>
                        )}
                    </div>
                </div>

                <div className="dashboard-grid">
                    {/* Replace map-container with billing cards */}
                    <div className="map-container billing-container">
                        <h2 className="map-title">
                            Bill History
                        </h2>

                        <div className="billing-content">
                            {!isLoading && billingData && (
                                role === 'OPERATOR'
                                    ? billingData.allTripBills?.length === 0
                                    : billingData.tripBills?.length === 0
                            ) && (
                                <div className="empty-message">
                                    <i className="fas fa-inbox"></i>
                                    <p>
                                        {role === 'OPERATOR'
                                            ? 'No trips in the system yet.'
                                            : 'No trips yet. Start riding to see your billing history!'
                                        }
                                    </p>
                                </div>
                            )}

                            {!isLoading && billingData && (
                                role === 'OPERATOR'
                                    ? billingData.allTripBills?.length > 0
                                    : billingData.tripBills?.length > 0
                            ) && (
                                <div className="trip-bills-list">
                                    {filteredBills.length === 0 ? (
                                        <div className="empty-message">
                                            <i className="fas fa-filter"></i>
                                            <p>No bills match the selected filters.</p>
                                        </div>
                                    ) : (
                                        currentBills.map((tripBill) => (
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
                                                {role === 'OPERATOR' && (
                                                    <>
                                                        <div className="detail-row">
                                                            <span className="detail-label">
                                                                <i className="fas fa-id-badge"></i> User ID
                                                            </span>
                                                            <span className="detail-value">
                                                                {tripBill.userId}
                                                            </span>
                                                        </div>
                                                        <div className="detail-row">
                                                            <span className="detail-label">
                                                                <i className="fas fa-user"></i> Rider Name
                                                            </span>
                                                            <span className="detail-value">
                                                                {tripBill.userFullName}
                                                            </span>
                                                        </div>
                                                        <div className="detail-row">
                                                            <span className="detail-label">
                                                                <i className="fas fa-envelope"></i> Email
                                                            </span>
                                                            <span className="detail-value">
                                                                {tripBill.userEmail}
                                                            </span>
                                                        </div>
                                                    </>
                                                )}
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
                                                <div className="detail-row">
                                                    <span className="detail-label">
                                                        <i className="fas fa-tag"></i> Pricing Plan
                                                    </span>
                                                    <span className="detail-value">
                                                        {tripBill.pricingStrategy}
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

                                            {/* Pay Now Button - Only show for riders with pending bills */}
                                            {role !== 'OPERATOR' && tripBill.billStatus === 'PENDING' && (
                                                <button
                                                    className="pay-now-btn"
                                                    onClick={() => handlePayNow(tripBill)}
                                                >
                                                    <i className="fas fa-credit-card"></i> Pay Now
                                                </button>
                                            )}
                                        </div>
                                    ))
                                    )}
                                </div>
                            )}
                            
                        </div>
                        {/* Pagination Controls */}
                        {filteredBills.length > itemsPerPage && (
                            <div className="pagination">
                                <button
                                    onClick={() => paginate(currentPage - 1)}
                                    disabled={currentPage === 1}
                                    className="pagination-btn"
                                >
                                    <i className="fas fa-chevron-left"></i> Previous
                                </button>
                                
                                <div className="pagination-numbers">
                                    {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                                        <button
                                            key={number}
                                            onClick={() => paginate(number)}
                                            className={`pagination-number ${currentPage === number ? 'active' : ''}`}
                                        >
                                            {number}
                                        </button>
                                    ))}
                                </div>

                                <button
                                    onClick={() => paginate(currentPage + 1)}
                                    disabled={currentPage === totalPages}
                                    className="pagination-btn"
                                >
                                    Next <i className="fas fa-chevron-right"></i>
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Sidebar with Payment Component */}
                    <div className="sidebar-container">
                        {selectedBill && role !== 'OPERATOR' ? (
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
                                    <div className="payment-detail-item">
                                        <span>Pricing Plan</span>
                                        <span>{selectedBill.pricingStrategy}</span>
                                    </div>
                                </div>

                                <div className="payment-breakdown">
                                    <h4>Payment Breakdown</h4>
                                    <div className="breakdown-item">
                                        <span>Base Fare</span>
                                        <span>${selectedBill.baseFare.toFixed(2)}</span>
                                    </div>
                                    <div className="breakdown-item">
                                        <span>
                                            {selectedBill.durationMinutes === 0 
                                                ? `Time Charge (${Math.round((new Date(selectedBill.endTime) - new Date(selectedBill.startTime)) / 1000)} sec × $${(selectedBill.perMinuteRate / 60).toFixed(4)})`
                                                : `Time Charge (${selectedBill.durationMinutes} min × $${selectedBill.perMinuteRate.toFixed(2)})`
                                            }
                                        </span>
                                        <span>${(selectedBill.regularCost - selectedBill.baseFare).toFixed(2)}</span>
                                    </div>
                                    {selectedBill.regularCost > selectedBill.totalAmount && (
                                        <div className="breakdown-item discount-row">
                                            <span>Total Savings (Tier + FlexMoney)</span>
                                            <span>-${(selectedBill.regularCost - selectedBill.totalAmount).toFixed(2)}</span>
                                        </div>
                                    )}
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
                                            value={paymentForm.cardNumber}
                                            onChange={(e) => handlePaymentFormChange('cardNumber', e.target.value)}
                                            required
                                            disabled={paymentProcessing}
                                        />
                                    </div>

                                    <div className="form-row">
                                        <div className="form-group">
                                            <label>Expiry Month</label>
                                            <input
                                                type="text"
                                                placeholder="MM"
                                                value={paymentForm.expiryMonth}
                                                onChange={(e) => handlePaymentFormChange('expiryMonth', e.target.value)}
                                                maxLength="2"
                                                required
                                                disabled={paymentProcessing}
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Expiry Year</label>
                                            <input
                                                type="text"
                                                placeholder="YY"
                                                value={paymentForm.expiryYear}
                                                onChange={(e) => handlePaymentFormChange('expiryYear', e.target.value)}
                                                maxLength="2"
                                                required
                                                disabled={paymentProcessing}
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>CVV</label>
                                            <input
                                                type="text"
                                                placeholder="123"
                                                value={paymentForm.cvc}
                                                onChange={(e) => handlePaymentFormChange('cvc', e.target.value)}
                                                maxLength="3"
                                                required
                                                disabled={paymentProcessing}
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
                                            value={paymentForm.cardHolderName}
                                            onChange={(e) => handlePaymentFormChange('cardHolderName', e.target.value)}
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
                                                    <i className="fas fa-credit-card"></i> Pay ${selectedBill.totalAmount.toFixed(2)}
                                                </>
                                            )}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        ) : (
                            <div className="no-reservation-card">
                                <h3>
                                    <i className="fas fa-info-circle"></i> {role === 'OPERATOR' ? 'System Summary' : 'Account Summary'}
                                </h3>
                                {role !== 'OPERATOR' && (
                                    <p className="helper-text">
                                        Select an unpaid bill to make a payment
                                    </p>
                                )}
                                {billingData && (
                                    <>
                                        <div className="summary-divider"></div>
                                        <p className="summary-stat">
                                            <strong>Total Trips:</strong> {role === 'OPERATOR' ? billingData.totalSystemTrips : billingData.totalTrips}
                                        </p>
                                        {role !== 'OPERATOR' && (
                                            <p className="summary-stat">
                                                <strong>Total Spent:</strong> ${billingData.totalAmountSpent.toFixed(2)}
                                            </p>
                                        )}
                                        {role === 'OPERATOR' && (
                                            <p className="summary-stat">
                                                <strong>Total Revenue:</strong> ${billingData.totalSystemRevenue.toFixed(2)}
                                            </p>
                                        )}
                                        <div className="summary-divider"></div>
                                        <p className="summary-stat">
                                            <strong>Outstanding Bills:</strong> {role === 'OPERATOR' ? billingData.totalSystemOutstandingBills : billingData.totalOutstandingBills}
                                        </p>
                                        <p className="summary-stat">
                                            <strong>Amount Due:</strong> ${role === 'OPERATOR' ? billingData.totalSystemOutstandingAmount.toFixed(2) : billingData.totalOutstandingAmount.toFixed(2)}
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

