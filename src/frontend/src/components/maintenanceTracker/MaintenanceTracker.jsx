import { React, useState } from 'react';
import './MaintenanceTracker.css';

const MaintenanceTracker = ({ bikesUnderMaintenance, activeBikeMaintenanceRemoval, setActiveBikeMaintenanceRemoval }) => {

    const handleRemoveClick = (bikeId) => {
        setActiveBikeMaintenanceRemoval(bikeId);
    };

    const handleCancelRemoval = () => {
        setActiveBikeMaintenanceRemoval(null);
    };

    return (
        <div className="maintenance-card">
            <div className="maintenance-header">
                <h3>Bikes Under Maintenance</h3>
                <span className="maintenance-count">{bikesUnderMaintenance.length}</span>
            </div>
            
            {bikesUnderMaintenance.length > 0 ? (
                <div className="maintenance-list">
                    {bikesUnderMaintenance.map((bike) => (
                        <div key={bike.bikeId} className="maintenance-item-wrapper">
                            <div className="maintenance-item">
                                <div className="maintenance-bike-icon">🚲</div>
                                <div className="maintenance-bike-details">
                                    <div className="maintenance-bike-id">
                                        Bike #{bike.bikeId}
                                    </div>
                                    <div className="maintenance-bike-type">
                                        Bike Type: {bike.bikeType}
                                    </div>
                                </div>
                                
                                {activeBikeMaintenanceRemoval === bike.bikeId ? (
                                    <button 
                                        className="cancel-maintenance-btn"
                                        onClick={handleCancelRemoval}
                                    >
                                        Cancel
                                    </button>
                                ) : (
                                    <button 
                                        className="remove-maintenance-btn"
                                        onClick={() => handleRemoveClick(bike.bikeId)}
                                    >
                                        Remove from Maintenance
                                    </button>
                                )}
                                
                                {activeBikeMaintenanceRemoval === bike.bikeId && (
                                <div className="removal-message">
                                    <div className="removal-message-icon">⚠️</div>
                                    <div className="removal-message-content">
                                        <p className="removal-message-title">Return this bike to service?</p>
                                        <p className="removal-message-text">
                                            Select an empty dock on the map to place this bike back into circulation.
                                        </p>
                                    </div>
                                </div>
                            )}
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="no-maintenance">
                    <p>All bikes are operational</p>
                    <p className="helper-text">No bikes currently under maintenance</p>
                </div>
            )}
        </div>
    );
};

export default MaintenanceTracker;