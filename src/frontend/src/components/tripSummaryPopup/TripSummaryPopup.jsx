import React from 'react';
import './TripSummaryPopup.css';

const TripSummaryPopup = ({ tripSummary, onClose }) => {
    if (!tripSummary) return null;

    // Check if there's a discount applied
    const loyaltyPercentage = parseFloat(tripSummary.loyaltyDiscount) || 0;
    const regularCost = parseFloat(tripSummary.regularCost) || 0;
    const discountAmount = regularCost * loyaltyPercentage;
    
    // Check if flexmoney was used
    const flexMoneyUsedRaw = parseFloat(tripSummary.flexMoneyUsed) || 0;

    // Visibility flags - only show if value is at least 1 cent (0.005 rounds up to 0.01)
    const showLoyalty = loyaltyPercentage > 0.001 && discountAmount > 0.005;
    const showFlexMoney = flexMoneyUsedRaw > 0.005;

    console.log('Debug Flags:', { showLoyalty, showFlexMoney, loyaltyPercentage, discountAmount, flexMoneyUsedRaw });

    // Check if flexmoney was earned
    const amountFlexMoneyEarned = tripSummary.flexMoneyEarned && tripSummary.flexMoneyEarned > 0 ? tripSummary.flexMoneyEarned : 0;

    // The final price to display (always use discountedCost if available)
    const finalPrice = tripSummary.discountedCost || 0;

    const durationSeconds = ((tripSummary.durationMinutes || 0) * 60).toFixed(0);

    // DEBUG
    console.log('Trip Summary Data:', tripSummary);
    console.log('regularCost:', tripSummary.regularCost);
    console.log('discountedCost:', tripSummary.discountedCost);
    console.log('finalPrice:', finalPrice);

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
                        <span className="trip-summary-label">Rider Name</span>
                        <span className="trip-summary-value">{tripSummary.userFullName}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Email</span>
                        <span className="trip-summary-value">{tripSummary.userEmail}</span>
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
                            {durationSeconds < 60 ? (
                                <span className="trip-summary-value">{durationSeconds} seconds</span>
                            ) : (
                            <span className="trip-summary-value">{tripSummary.durationMinutes} minutes</span>
                        )}
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

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Pricing Type</span>
                        <span className="trip-summary-value">{tripSummary.pricingStrategy}</span>
                    </div>

                    <div className="divider"></div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Base Fare</span>
                        <span className="trip-summary-value">${tripSummary.baseFare?.toFixed(2) || 'NULL'}</span>
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Rate</span>
                        { durationSeconds < 60 ? (
                            <span className="trip-summary-value">${((tripSummary.perMinuteRate || 0) / 60).toFixed(4) || 'NULL'}/sec</span>
                            ) : (
                            <span className="trip-summary-value">${tripSummary.perMinuteRate?.toFixed(2) || 'NULL'}/min</span>
                        )}
                    </div>

                    <div className="trip-summary-row">
                        <span className="trip-summary-label">Duration Charge</span>
                        
                        { durationSeconds < 60 ? (
                            <span className="trip-summary-value">
                                ${((tripSummary.perMinuteRate || 0) / 60 * durationSeconds)?.toFixed(2) || 'NULL'}
                            </span>
                            ) : (
                            <span className="trip-summary-value">
                                ${((tripSummary.perMinuteRate || 0) * (tripSummary.durationMinutes || 0))?.toFixed(2) || 'NULL'}
                            </span>
                            )}
                    </div>

                    {(showLoyalty || showFlexMoney) && (
                        <>
                            <div className="divider"></div>
                            
                            <div className="trip-summary-row">
                                <span className="trip-summary-label">Subtotal</span>
                                <span className="trip-summary-value">
                                    ${regularCost.toFixed(2)}
                                </span>
                            </div>

                            {showLoyalty && (
                                <div className="trip-summary-row discount-row">
                                    <span className="trip-summary-label">
                                        Loyalty Discount ({(loyaltyPercentage * 100).toFixed(0)}%)
                                    </span>
                                    <span className="trip-summary-value discount-value">
                                        -${discountAmount.toFixed(2)}
                                    </span>
                                </div>
                            )}

                            {showFlexMoney && (
                                <div className="trip-summary-row discount-row">
                                    <span className="trip-summary-label">
                                        FlexMoney Used
                                    </span>
                                    <span className="trip-summary-value discount-value">
                                        -${flexMoneyUsedRaw.toFixed(2)}
                                    </span>
                                </div>
                            )}
                        </>
                        )}

                    <div className="divider"></div>
                    
                    {tripSummary.flexMoneyEarned > 0 && (
                        <div className="trip-summary-row flex-earned-row">
                            <span className="trip-summary-label">
                                FlexMoney Earned
                            </span>
                            <span className="trip-summary-value flex-earned-value">
                                +{(amountFlexMoneyEarned / 100).toFixed(2)}
                            </span>
                        </div>
                    )}

                    <div className="trip-summary-total">
                        <span>TOTAL</span>
                        <span>${finalPrice.toFixed(2)}</span>
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

