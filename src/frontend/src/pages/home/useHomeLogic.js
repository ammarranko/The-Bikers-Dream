import { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function useHomeLogic() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [loadingMessage, setLoadingMessage] = useState('');
    const [retryCount, setRetryCount] = useState(0);
    const [isConnected, setIsConnected] = useState(false);

    // Constants for SSE reconnection
    const MAX_RETRIES = 5;
    const RETRY_DELAY = 2000; // 2 seconds base delay

    const token = localStorage.getItem('jwt_token');
    if (token && token !== "null") {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete axios.defaults.headers.common['Authorization'];
    }

    const [confirmReservation, setConfirmReservation] = useState({ active: false, bike: null, station: null });
    const [showCancelReservationPopup, setShowCancelReservationPopup] = useState(false);
    const [confirmRental, setConfirmRental] = useState({ active: false, dock: null, bike: null, station: null });
    const [confirmReturn, setConfirmReturn] = useState({ active: false, dock: null, bike: null, station: null });

    const [activeBikeRental, setActiveBikeRental] = useState({ hasOngoingRental: false, bikeId: null, tripId: null, dock: null, station: null });
    const [activeReservation, setActiveReservation] = useState({ hasActiveReservation: false, bikeId: null, stationId: null, expiresAt: null, reservationId: null });
    const [activeBikeMaintenanceRemoval, setActiveBikeMaintenanceRemoval] = useState(null);
    const [stations, setStations] = useState([]);
    const [timeLeft, setTimeLeft] = useState(null);
    const [bikesUnderMaintenance, setBikesUnderMaintenance] = useState([]);

    const fullName = localStorage.getItem('user_full_name');
    const role = localStorage.getItem('user_role');
    let userEmail = localStorage.getItem('user_email');

    // Loading wrapper function
    const withLoading = async (message, operation) => {
        setLoadingMessage(message);
        setIsLoading(true);
        try {
            await operation();
        } finally {
            setIsLoading(false);
            setLoadingMessage('');
        }
    };

    // Handle pop-up actions
    const handleCancelConfirmationRental = () => setConfirmRental({ active: false, dock: null, bike: null, station: null });
    const onClickShowConfirmReturn = (dock, bike, station) => setConfirmReturn({ active: true, dock, bike, station });
    const handleCancelConfirmationReturn = () => setConfirmReturn({ active: false, dock: null, bike: null, station: null });
    const handleShowReservation = (bike, station) => setConfirmReservation({ active: true, bike, station });

    // Initial data loading
    useEffect(() => {
        const loadInitialData = async () => {
            await withLoading('Loading your bike information...', async () => {
                await Promise.all([
                    fetchStations(),
                    fetchActiveRental(),
                    fetchActiveReservation(),
                    fetchBikesUnderMaintenance()
                ]);
            });
        };
        loadInitialData();
    }, []);
    
    // SSE connection setup with auto-reconnect
    useEffect(() => {
        let retryTimer = null;
        const eventSource = new EventSource('http://localhost:8080/api/stations/subscribe', {
            withCredentials: true
        });

        // Connection established
        eventSource.onopen = () => {
            console.log('SSE connection established');
            setIsConnected(true);
            setRetryCount(0); // Reset retry count on successful connection
        };

        // Handle general messages
        eventSource.onmessage = (event) => {
            const data = JSON.parse(event.data);
            console.log('SSE update received:', data);
        };

        // Handle station-level updates
        eventSource.addEventListener('station-update', (event) => {
            const stationData = JSON.parse(event.data);
            console.log('Station update received:', stationData);
            setStations(currentStations => 
                currentStations.map(station => 
                    station.stationId === stationData.stationId ? stationData : station
                )
            );
        });

        // Handle dock-level updates
        eventSource.addEventListener('dock-update', (event) => {
            const dockUpdate = JSON.parse(event.data);
            console.log('Dock update received:', dockUpdate);
            setStations(currentStations => 
                currentStations.map(station => {
                    if (station.stationId === dockUpdate.stationId) {
                        return {
                            ...station,
                            docks: station.docks.map(dock => 
                                dock.dockId === dockUpdate.dock.dockId ? dockUpdate.dock : dock
                            )
                        };
                    }
                    return station;
                })
            );
        });

        // Error handling with auto-reconnect
        eventSource.onerror = (error) => {
            console.error('SSE connection error:', error);
            try { eventSource.close(); } catch (e) { /* ignore */ }
            setIsConnected(false);

            // Implement exponential backoff for retries by incrementing retryCount,
            // which will re-run this effect and create a new EventSource.
            if (retryCount < MAX_RETRIES) {
                const timeout = RETRY_DELAY * Math.pow(2, retryCount);
                console.log(`Retrying SSE connection in ${timeout}ms (attempt ${retryCount + 1}/${MAX_RETRIES})`);
                retryTimer = setTimeout(() => {
                    setRetryCount(prev => prev + 1);
                }, timeout);
            } else {
                console.error('Max SSE retry attempts reached');
            }
        };

        // Cleanup on component unmount or before re-running effect
        return () => {
            console.log('Cleaning up SSE connection');
            try { eventSource.close(); } catch (e) { /* ignore */ }
            if (retryTimer) {
                clearTimeout(retryTimer);
                retryTimer = null;
            }
        };
    }, [retryCount]); // Re-run effect when retryCount changes to handle reconnection

    // Reservation timer effect
    useEffect(() => {
        if (!activeReservation?.expiresAt || !activeReservation.hasActiveReservation) {
            setTimeLeft(null);
            return;
        }

        const interval = setInterval(async () => {
            const now = new Date();
            const expiry = new Date(activeReservation.expiresAt);
            const diffMs = expiry - now;
            
            if (diffMs <= 0) {
                const reservationId = activeReservation.reservationId;
                clearInterval(interval);
                setActiveReservation({ hasActiveReservation: false, bikeId: null, stationId: null, expiresAt: null, reservationId: null });
                setTimeLeft(null);

                // Notify backend that reservation expired
                if (activeReservation.reservationId) {
                    await axios.post("http://localhost:8080/api/reservations/end", 
                        { 
                            reservationId,  
                            userEmail
                        },
                        { params: { type: 'EXPIRE' } }
                    );
                }

                await Promise.all([fetchStations(), fetchActiveRental()]);
            } else {
                const minutes = Math.floor(diffMs / 60000);
                const seconds = Math.floor((diffMs % 60000) / 1000);
                setTimeLeft(`${minutes}m ${seconds}s`);
            }
        }, 1000);
        
        return () => clearInterval(interval);
    }, [activeReservation]);

    // API Operations
    const toggleStationStatus = async (stationId, currentStatus) => {
        await withLoading('Updating station status...', async () => {
            const newStatus = currentStatus === "ACTIVE" ? "OUT_OF_SERVICE" : "ACTIVE";
            try {
                await axios.post('http://localhost:8080/api/operator/stations/status', { stationId, status: newStatus });
                await fetchStations();
            } catch (error) {
                console.error("Error changing station status:", error);
                if (error.response?.status === 401) {
                    alert("Unauthorized. Please login again.");
                    handleLogout();
                } else {
                    alert(`Failed updating station status: ${error.response?.data || error.message}`);
                }
            }
        });
    };

    const rebalanceBike = async (rebalanceData) => {
        await withLoading('Rebalancing bike...', async () => {
            try {
                await axios.post(`http://localhost:8080/api/operator/rebalance`, rebalanceData);
                await fetchStations();
            } catch (error) {
                console.error("Error rebalancing bike:", error);
                if (error.response?.status === 401) {
                    alert("Unauthorized. Please login again.");
                    handleLogout();
                } else {
                    alert(`Failed rebalancing bike: ${error.response?.data || error.message}`);
                }
            }
        });
    };

    const fetchActiveReservation = async () => {
        try {
            const response = await axios.post("http://localhost:8080/api/reservations/check", { userEmail });
            if (response.data.hasActiveReservation) {
                setActiveReservation({
                    hasActiveReservation: true,
                    bikeId: response.data.bikeId,
                    stationId: response.data.stationId,
                    expiresAt: response.data.expiresAt,
                    reservationId: response.data.reservationId
                });
            } else {
                setActiveReservation({
                    hasActiveReservation: false,
                    bikeId: null,
                    stationId: null,
                    expiresAt: null,
                    reservationId: null
                });
            }
        } catch (error) {
            console.error("Error checking reservation:", error);
            setActiveReservation({
                hasActiveReservation: false,
                bikeId: null,
                stationId: null,
                expiresAt: null,
                reservationId: null
            });
        }
    };

    const fetchActiveRental = async () => {
        const responseData = await checkRental();
        setActiveBikeRental({
            hasOngoingRental: responseData.hasOngoingRental,
            bikeId: responseData.bikeId,
            tripId: responseData.tripId,
            dock: responseData.dock,
            station: responseData.station
        });
    };

    const fetchStations = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/stations/allStations/details");
            setStations(response.data);
        } catch (error) {
            console.error("Error fetching stations:", error);
            setStations([]);
        }
    };

    const fetchBikesUnderMaintenance = async () => {
        try {
            const response = await axios.get("http://localhost:8080/api/operator/maintenance/bikes_under_maintenance");
            setBikesUnderMaintenance(response.data);
        } catch (error) {
            console.error("Error fetching bikes under maintenance:", error);
            setBikesUnderMaintenance([]);
        }
    };

    const checkRental = async () => {
        try {
            const response = await axios.post("http://localhost:8080/api/trips/checkRental", { userEmail });
            if (response.data.hasOngoingRental) {
                return {
                    hasOngoingRental: true,
                    bikeId: response.data.bikeId,
                    tripId: response.data.tripId,
                    dock: response.data.dock,
                    station: response.data.station
                };
            }
            return { hasOngoingRental: false, bikeId: null, tripId: null, dock: null, station: null };
        } catch (error) {
            console.error("Error checking rental:", error);
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            }
            return { hasOngoingRental: false, bikeId: null, tripId: null, dock: null, station: null };
        }
    };

    const handleConfirmReservation = async () => {
        setConfirmReservation({ active: false, bike: null, station: null });
        await withLoading('Creating your reservation...', async () => {
            const bikeId = confirmReservation.bike.bikeId;
            const stationId = confirmReservation.station.stationId;
            try {
                const response = await axios.post("http://localhost:8080/api/reservations/create", { bikeId, stationId, userEmail });
                if (response.data) {
                    setActiveReservation({
                        hasActiveReservation: true,
                        bikeId,
                        stationId,
                        expiresAt: response.data.expiresAt,
                        reservationId: response.data.reservationId
                    });
                    await fetchStations();
                }
            } catch (error) {
                console.error("Error creating reservation:", error);
                alert(`Failed to create reservation: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleCancelActiveReservation = async () => {
        await withLoading('Cancelling your reservation...', async () => {
            try {
                const reservationId = Number(activeReservation.reservationId);
                if (!reservationId) {
                    alert("No reservation to cancel.");
                    return;
                }
                await axios.post("http://localhost:8080/api/reservations/end", 
                    { 
                        reservationId: activeReservation.reservationId, 
                        userEmail
                    },
                    { params: { type: 'CANCEL' } }
                );
                setActiveReservation({
                    hasActiveReservation: false,
                    bikeId: null,
                    stationId: null,
                    expiresAt: null,
                    reservationId: null
                });
                setTimeLeft(null);
                await fetchStations();
            } catch (error) {
                console.error("Error cancelling reservation:", error);
                alert(`Failed to cancel reservation: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleLogout = () => {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_email');
        localStorage.removeItem('user_full_name');
        localStorage.removeItem('user_role');
        delete axios.defaults.headers.common['Authorization'];
        navigate('/login?logout=1', { replace: true });
    };

    const handleConfirmRental = async () => {
        setConfirmRental({ active: false, dock: null, bike: null, station: null });
        await withLoading('Processing your rental...', async () => {
            const bikeId = confirmRental.bike.bikeId;
            const dockId = confirmRental.dock.dockId;
            const stationId = confirmRental.station.stationId;
            try {
                // Cancel reservation first if one exists
                const reservationId = Number(activeReservation.reservationId);
                if (reservationId) {
                    await axios.post("http://localhost:8080/api/reservations/end", 
                        { 
                            reservationId: activeReservation.reservationId, 
                            userEmail
                        },
                        { params: { type: 'CANCEL' } }
                    );
                }
                
                // Then confirm rental
                await axios.post("http://localhost:8080/api/trips/rent", {
                    bikeId,
                    userEmail,
                    dockId,
                    stationId
                });
                setActiveReservation({
                        hasActiveReservation: false,
                        bikeId: null,
                        stationId: null,
                        expiresAt: null,
                        reservationId: null
                });
                setTimeLeft(null);
                await Promise.all([fetchStations(), fetchActiveRental()]);
            } catch (error) {
                console.error("Error renting bike:", error);
                alert(`Failed to rent bike: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleConfirmReturn = async () => {
        setConfirmReturn({ active: false, dock: null, bike: null, station: null });
        await withLoading('Processing your return...', async () => {
            const tripId = activeBikeRental.tripId
            const bikeId = activeBikeRental.bikeId;
            const dockId = confirmReturn.dock.dockId;
            const stationId = confirmReturn.station.stationId;
            try {
                await axios.post("http://localhost:8080/api/trips/return", {
                    userEmail,
                    tripId,
                    dockId,
                    bikeId,
                    stationId
                });
                setActiveReservation({
                    hasActiveReservation: false,
                    bikeId: null,
                    stationId: null,
                    expiresAt: null,
                    reservationId: null
                });
                setTimeLeft(null);
                await Promise.all([fetchStations(), fetchActiveRental()]);
            } catch (error) {
                console.error("Error returning bike:", error);
                alert(`Failed to return bike: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleBikeMaintain = async (bike) => {
        await withLoading('Updating bike maintenance status...', async () => {
            try {
                await axios.post('http://localhost:8080/api/operator/maintenance/set', { bikeId: bike.bikeId });

                setBikesUnderMaintenance(currentBikes => {
                    const isAlreadyUnderMaintenance = currentBikes.some(b => b.bikeId === bike.bikeId);
                    if (!isAlreadyUnderMaintenance) {
                        return [...currentBikes, bike];
                    }
                    return currentBikes;
                });
                
                await fetchStations();
            } catch (error) {
                console.error("Error updating bike maintenance status:", error);
                alert(`Failed to update bike maintenance status: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleRemoveFromMaintenance = async (bikeId, dockId) => {
        await withLoading('Removing bike from maintenance...', async () => {
            try {
                await axios.post('http://localhost:8080/api/operator/maintenance/remove', { bikeId, dockId });

                setBikesUnderMaintenance(currentBikes => {
                    return currentBikes.filter(b => b.bikeId !== bikeId);
                });

                await fetchStations();
            } catch (error) {
                console.error("Error removing bike from maintenance:", error);
                alert(`Failed to remove bike from maintenance: ${error.response?.data || error.message}`);
            }
        });
    };

    return {
        // Loading states
        isLoading,
        loadingMessage,
        // User info
        fullName,
        role,
        userRole: role,
        // Data
        stations,
        activeReservation,
        timeLeft,
        activeBikeRental,
        bikesUnderMaintenance,
        activeBikeMaintenanceRemoval, 
        // Popups & control
        confirmRental,
        confirmReturn,
        confirmReservation,
        showCancelReservationPopup,
        // Actions
        handleLogout,
        fetchStations,
        onClickShowConfirmRental: (dock, bike, station) => setConfirmRental({ active: true, dock, bike, station }),
        onClickShowConfirmReturn,
        handleShowReservation,
        onClickShowCancelReservation: () => setShowCancelReservationPopup(true),
        toggleStationStatus,
        rebalanceBike,
        handleConfirmReservation,
        setConfirmReservation,
        handleCancelActiveReservation,
        setShowCancelReservationPopup,
        handleConfirmRental,
        handleCancelConfirmationRental,
        handleConfirmReturn,
        handleCancelConfirmationReturn,
        handleBikeMaintain,
        handleRemoveFromMaintenance,
        setActiveBikeMaintenanceRemoval
    };
}