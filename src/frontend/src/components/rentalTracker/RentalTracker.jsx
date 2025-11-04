import React from 'react';

const RentalTracker = ({ activeBikeRental }) => {
    return (
        <div className="rental-tracker active-rental">
            <h3>Current Rental</h3>
            <div className="rental-info">
                <p><strong>Bike ID:</strong> {activeBikeRental.bikeId}</p>
                <p><strong>Status:</strong> Active</p>
                {activeBikeRental.startTime && (
                    <p>
                        <strong>Started:</strong> {' '}
                        {new Date(activeBikeRental.startTime).toLocaleTimeString()}
                    </p>
                )}
            </div>
            <p className="helper-text">Return your bike at any available station when you're done!</p>
        </div>
    );
};

export default RentalTracker;