import React from 'react';
import { useNavigate } from 'react-router-dom';
import Map from '../../components/Map';
import useHomeLogic from './useHomeLogic';
import ReservationBanner from '../../components/reservationBanner/ReservationBanner';
import RentalTracker from '../../components/rentalTracker/RentalTracker';
import NavigationBar from '../../components/navigationBar/NavigationBar';
import PopupManager from '../../components/PopupManager';
import LoadingSpinner from '../../components/loadingSpinner/LoadingSpinner';
import './Home.css';

const Home = () => {
    const navigate = useNavigate();

    const {
        // Loading states
        isLoading,
        loadingMessage,
        // Data
        fullName,
        role,
        stations,
        activeReservation,
        timeLeft,
        activeBikeRental,
        tripSummaryData,
        // popups & control
        confirmRental,
        rentalSuccessPopup,
        confirmReturn,
        returnSuccessPopup,
        confirmReservation,
        reservationSuccessPopup,
        showCancelReservationPopup,
        // actions
        handleLogout,
        fetchStations,
        onClickShowConfirmRental,
        onClickShowConfirmReturn,
        handleShowReservation,
        onClickShowCancelReservation,
        toggleStationStatus,
        userRole,
        rebalanceBike,
        handleConfirmReservation,
        setConfirmReservation,
        setReservationSuccessPopup,
        handleCancelActiveReservation,
        setShowCancelReservationPopup,
        handleConfirmRental,
        handleCancelConfirmationRental,
        handleCancelEventRental,
        handleConfirmReturn,
        handleCancelConfirmationReturn,
        handleCancelEventReturn
    } = useHomeLogic();

    const mapProps = {
        onClickShowConfirmRental,
        activeBikeRental,
        onClickShowConfirmReturn,
        stations,
        setStations: fetchStations,
        onClickShowConfirmReservation: handleShowReservation,
        activeReservation,
        onClickShowCancelReservation,
        toggleStationStatus,
        userRole,
        rebalanceBike
    };

    const popupProps = {
        confirmRental,
        rentalSuccessPopup,
        confirmReturn,
        returnSuccessPopup,
        confirmReservation,
        reservationSuccessPopup,
        showCancelReservationPopup,
        activeBikeRental,
        tripSummaryData,
        handleConfirmRental,
        handleCancelConfirmationRental,
        handleCancelEventRental,
        handleConfirmReturn,
        handleCancelConfirmationReturn,
        handleCancelEventReturn,
        handleConfirmReservation,
        setConfirmReservation,
        setReservationSuccessPopup,
        handleCancelActiveReservation,
        setShowCancelReservationPopup
    };

    const handleBillingClick = () => {
        navigate('/billing');
    };

    return (
        <div className="home-container">
            {isLoading && <LoadingSpinner message={loadingMessage} />}
            
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
                            `Welcome back, ${fullName.split(' ')[0]}!`
                        ) : (
                            'Welcome to BikeShare'
                        )}
                    </h1>
                    <p className="welcome-subtitle">
                        {role === 'OPERATOR' ? (
                            'Manage stations and monitor bike rentals'
                        ) : (
                            'Ready to start your journey? Find and reserve a bike near you.'
                        )}
                    </p>
                </div>

                <div className="dashboard-grid">
                    <div className="map-container">
                        <h2 className="map-title">
                            {stations && stations.length > 0 ? (
                                `Available Stations (${stations.length})`
                            ) : (
                                'Loading Stations...'
                            )}
                        </h2>
                        <Map {...mapProps} {...popupProps} />
                    </div>

                    <div className="sidebar-container">
                        <div className="reservation-section">
                            {activeReservation.hasActiveReservation ? (
                                <div className="reservation-card">
                                    <ReservationBanner 
                                        activeReservation={activeReservation} 
                                        timeLeft={timeLeft} 
                                    />
                                </div>
                            ) : (
                                <div className="no-reservation-card">
                                    <h3>No Active Reservations</h3>
                                    <p>You currently don't have any bike reservations.</p>
                                    <p className="helper-text">Click on a station marker to reserve a bike!</p>
                                </div>
                            )}
                        </div>
                        <div className="rental-section">
                                {activeBikeRental.hasOngoingRental && activeBikeRental.bikeId ? (
                                    <div className="reservation-card">
                                        <RentalTracker activeBikeRental={activeBikeRental} />
                                    </div>
                                ) : (
                                    <div className="no-reservation-card">
                                        <h3>No Active Rentals</h3>
                                        <p>You don't have any bikes rented at the moment.</p>
                                        <p className="helper-text">Find an available bike on the map to start riding!</p>
                                    </div>
                                )}
                                
                        </div>
                    </div>
                </div>
            </div>
            <PopupManager {...popupProps} />
        </div>
    );
};

export default Home;