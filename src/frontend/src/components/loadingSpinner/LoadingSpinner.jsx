import React from 'react';
import './LoadingSpinner.css';

const LoadingSpinner = ({ message = 'Loading...' }) => {
    return (
        <div className="loading-overlay">
            <div>
                <div className="loading-spinner" />
                <div className="loading-text">{message}</div>
            </div>
        </div>
    );
};

export default LoadingSpinner;