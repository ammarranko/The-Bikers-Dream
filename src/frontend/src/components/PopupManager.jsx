import React from 'react';
import ConfirmationPopup from "./confirmationPopup/ConfirmationPopup";
import EventPopup from "./eventPopup/EventPopup";

const PopupManager = ({
    confirmRental,
    rentalSuccessPopup,
    confirmReturn,
    returnSuccessPopup,
    confirmReservation,
    reservationSuccessPopup,
    showCancelReservationPopup,
    activeBikeRental,
    // handlers
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
    setShowCancelReservationPopup,
}) => {
    return (
        <>
            {confirmRental.active && (
                <ConfirmationPopup
                    message={`You are about to rent a bike! 🚲\n\nStation: ${confirmRental.station.stationName} (ID: ${confirmRental.station.stationId})\nDock: ${confirmRental.dock.dockId}\nBike: ${confirmRental.bike.bikeId}\n\nDo you want to proceed with this rental?`}
                    onConfirm={handleConfirmRental}
                    onCancel={handleCancelConfirmationRental}
                />
            )}

            {rentalSuccessPopup && (
                <EventPopup
                    message={`Bike Rental Successful! 🚲`}
                    onCancel={handleCancelEventRental}
                />
            )}

            {confirmReturn.active && (
                <ConfirmationPopup
                    message={`You are about to return a bike! 🔙🚲\n\nStation: ${confirmReturn.station.stationName} (ID: ${confirmReturn.station.stationId})\nDock: ${confirmReturn.dock.dockId}\nBike: ${activeBikeRental.bikeId}\n\nDo you want to proceed with this return?`}
                    onConfirm={handleConfirmReturn}
                    onCancel={handleCancelConfirmationReturn}
                />
            )}

            {returnSuccessPopup && (
                <EventPopup
                    message={`Bike Return Successful! 🔙🚲`}
                    onCancel={handleCancelEventReturn}
                />
            )}

            {confirmReservation.active && (
                <ConfirmationPopup
                    message={`Do you want to reserve this bike? Bike ID: ${confirmReservation.bike.bikeId}, Station: ${confirmReservation.station.stationName}`}
                    onConfirm={handleConfirmReservation}
                    onCancel={() => setConfirmReservation({ active: false, bike: null, station: null })}
                />
            )}

            {reservationSuccessPopup && (
                <EventPopup
                    message={`Bike Reservation Successful! 🚲`}
                    onCancel={() => setReservationSuccessPopup(false)}
                />
            )}

            {showCancelReservationPopup && (
                <ConfirmationPopup
                    message={`Are you sure you want to cancel your current reservation?`}
                    onConfirm={() => {handleCancelActiveReservation(); setShowCancelReservationPopup(false);}}
                    onCancel={() => setShowCancelReservationPopup(false)}
                />
            )}
        </>
    );
};

export default PopupManager;