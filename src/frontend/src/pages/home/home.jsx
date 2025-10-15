import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Map from '../../components/Map'
import ConfirmationPopup from "../../components/confirmationPopup/ConfirmationPopup";

const Home = () => {
    const navigate = useNavigate();

    // State to track whether the user wants to confirm and associated variables
    const [confirmRental, setConfirmRental] = useState(null);

    // Retrieve full name from localStorage
    const fullName = localStorage.getItem('user_full_name');
    const role = localStorage.getItem('user_role');

    const handleLogout = () => {
        try {
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_email');
            localStorage.removeItem('user_full_name');
            localStorage.removeItem('user_role');
            delete axios.defaults.headers.common['Authorization'];
        } finally {
            // Navigate to login page and trigger auth logout handling
            navigate('/login?logout=1', { replace: true });
        }
    };

    /*
        --- Rental Confirmation Logic ---
    */
    const onClickShowConfirm = (dock, bike, station) => {
        setConfirmRental({dock, bike, station});
    };

    const handleConfirm = async () => {
        try {
            const response = await axios.post("");
            console.log("Received rental response with data: ", response.data)
        } catch (error) {
            console.error("Error in POST for rental:", error);
        }
    };

    const handleCancel = () => {
        setConfirmRental(null);
    };

    return (
        <div style={{ padding: '16px' }}>
            <header style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <h1>Hello{fullName ? `, ${fullName}` : ''}!</h1>
                <h1>Hello{role ? `, ${role}` : ''}!</h1>

                <button type="button" onClick={handleLogout} style={{padding: '8px 12px', cursor: 'pointer'}}>
                    Logout
                </button>
            </header>

            <main>
                <p>Welcome to the app.</p>
                <Map onClickShowConfirm={onClickShowConfirm} />

                {confirmRental && (
                    <ConfirmationPopup
                        message={`You are about to rent a bike! 🚲

                                    Station: ${confirmRental.station.stationName} (ID: ${confirmRental.station.stationId})
                                    Dock: ${confirmRental.dock.dockId}
                                    Bike: ${confirmRental.bike.bikeId}

                                    Do you want to proceed with this rental?`
                                }
                        onConfirm={handleConfirm}
                        onCancel={handleCancel}
                    />
                )}
            </main>

            
        </div>
    );
};

export default Home;
