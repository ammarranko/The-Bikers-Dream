import { useState, useEffect, useRef } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function useHomeLogic() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [loadingMessage, setLoadingMessage] = useState('');
    const [retryCount, setRetryCount] = useState(0);
    const [operatorRetryCount, setOperatorRetryCount] = useState(0);
    const [isConnected, setIsConnected] = useState(false);
    const [operatorIsConnected, setOperatorIsConnected] = useState(false);
    const [operatorEvents, setOperatorEvents] = useState([]) // for operator console

    // Constants for SSE reconnection
    const MAX_RETRIES = 5;
    const RETRY_DELAY = 2000;

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

    // Popup states
    const [rentalSuccessPopup, setRentalSuccessPopup] = useState(false);
    const [returnSuccessPopup, setReturnSuccessPopup] = useState(false);
    const [reservationSuccessPopup, setReservationSuccessPopup] = useState(false);
    const [tripSummaryData, setTripSummaryData] = useState(null);
    const [reservationExpiredPopup, setReservationExpiredPopup] = useState(false);
    const [showTierPopup, setShowTierPopup] = useState(false);
    const [tierChangeData, setTierChangeData] = useState({ oldTier: "", newTier: "" });


    const fullName = localStorage.getItem('user_full_name');
    const role = localStorage.getItem('user_role');
    let userEmail = localStorage.getItem('user_email');

    // Ref to keep track of the last seen tier
    const lastTierRef = useRef(localStorage.getItem('tier'));

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
    const handleCancelEventRental = () => setRentalSuccessPopup(false);
    const onClickShowConfirmReturn = (dock, bike, station) => setConfirmReturn({ active: true, dock, bike, station });
    const handleCancelConfirmationReturn = () => setConfirmReturn({ active: false, dock: null, bike: null, station: null });
    const handleCancelEventReturn = () => {setReturnSuccessPopup(false); setTripSummaryData(null);
    };
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

    // SSE connection setup for Operator with auto-reconnect
    useEffect(() => {
        if (role === 'OPERATOR') {
            let operatorEventSource = null;
            let operatorRetryTimer = null;

            const connect = () => {
                operatorEventSource = new EventSource('http://localhost:8080/api/events/subscribe', {
                    withCredentials: true
                });
            

                // Connection established
                operatorEventSource.onopen = () => {
                    console.log('Operator SSE connection established');
                    setOperatorIsConnected(true);
                    setOperatorRetryCount(0);
                };

                // Handle general messages
                operatorEventSource.onmessage = (event) => {
                    const data = JSON.parse(event.data);
                    console.log('Operator SSE update received:', data);
                };

                // Handle operator events
                operatorEventSource.addEventListener('operator-event', (event) => {
                    const eventData = JSON.parse(event.data);
                    console.log('OPERATOR EVENT RECEIVED:', eventData);
                    
                    // Store operator events for the console
                    setOperatorEvents(prev => [eventData, ...prev]);
                });

                // Handle connection events
                operatorEventSource.addEventListener('connected', (event) => {
                    console.log('Operator SSE connected event:', event.data);
                });

                // Error handling with auto-reconnect
                operatorEventSource.onerror = () => {
                    console.error('SSE connection error');
                    operatorEventSource.close();
                    setIsConnected(false);

                    if (retryCount < MAX_RETRIES) {
                        const timeout = RETRY_DELAY * Math.pow(2, operatorRetryCount);
                        console.log(`Retrying SSE in ${timeout}ms`);
                        operatorRetryTimer = setTimeout(() => {
                            setRetryCount(prev => prev + 1);
                            connect(); // re-establish manually
                        }, timeout);
                    }
                };
            };

            connect();

            // Cleanup on component unmount or before re-running effect
            return () => {
                console.log('Cleaning up Operator SSE connection');
                try { operatorEventSource.close(); } catch (e) { /* ignore */ }
                if (operatorRetryTimer) {
                    clearTimeout(operatorRetryTimer);
                    operatorRetryTimer = null;
                }
            };
        }
    }, []);

    // SSE connection setup with auto-reconnect
    useEffect(() => {
        let eventSource = null;
        let retryTimer = null;

        const connect = () => {
            eventSource = new EventSource('http://localhost:8080/api/stations/subscribe', {
                withCredentials: true
            });

            // Connection established
            eventSource.onopen = () => {
                console.log('SSE connection established');
                setIsConnected(true);
                setRetryCount(0);
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

            // Handle maintenance-specific updates
            eventSource.addEventListener('maintenance-update', (event) => {
                const maintenanceData = JSON.parse(event.data);
                console.log('Maintenance update received:', maintenanceData);
                
                setBikesUnderMaintenance(currentBikes => {
                    if (maintenanceData.action === 'ADDED') {
                        // Add bike to maintenance list if not already there
                        const isAlreadyInList = currentBikes.some(b => b.bikeId === maintenanceData.bikeId);
                        if (!isAlreadyInList) {
                            console.log('DEBUG: Adding bike to maintenance list:', maintenanceData.bikeId);
                            return [...currentBikes, {
                                bikeId: maintenanceData.bikeId,
                                status: maintenanceData.bikeStatus
                            }];
                        }
                        return currentBikes;
                    } else if (maintenanceData.action === 'REMOVED') {
                        console.log('DEBUG: Removing bike from maintenance list:', maintenanceData.bikeId);
                        // Remove bike from maintenance list
                        return currentBikes.filter(b => b.bikeId !== maintenanceData.bikeId);
                    }
                    return currentBikes;
                });
            });

            // Handle connection events
            eventSource.addEventListener('connected', (event) => {
                console.log('SSE connected event:', event.data);
            });

            // Error handling with auto-reconnect
            eventSource.onerror = () => {
                console.error('SSE connection error');
                eventSource.close();
                setIsConnected(false);

                if (retryCount < MAX_RETRIES) {
                    const timeout = RETRY_DELAY * Math.pow(2, retryCount);
                    console.log(`Retrying SSE in ${timeout}ms`);
                    retryTimer = setTimeout(() => {
                        setRetryCount(prev => prev + 1);
                        connect(); // re-establish manually
                    }, timeout);
                }
            };
        };

        connect();

        // Cleanup on component unmount or before re-running effect
        return () => {
            console.log('Cleaning up SSE connection');
            try { eventSource.close(); } catch (e) { /* ignore */ }
            if (retryTimer) {
                clearTimeout(retryTimer);
                retryTimer = null;
            }
        };
    }, []);

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
                // Show popup for expired reservation
                setReservationExpiredPopup(true);

                if (activeReservation.reservationId) {
                    const response = await axios.post("http://localhost:8080/api/reservations/end",
                        {
                            reservationId,  
                            userEmail
                        },
                        { params: { type: 'EXPIRE' } }
                    );

                    // Update tier in localStorage if returned from backend
                    if (response.data.userTier) {
                        localStorage.setItem('tier', response.data.userTier);
                        console.log('Tier updated after reservation expiration:', response.data.userTier);
                        // Trigger custom event to notify other components
                        window.dispatchEvent(new Event('tierUpdated'));
                    }
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

    // Window alert upon tier change (while logged in)
    // Added interval so that constantly checking
    useEffect(() => {
        const checkTierChange = () => {
            const currentTier = localStorage.getItem('tier');
            if (lastTierRef.current && currentTier && lastTierRef.current !== currentTier) {
                setTierChangeData({ oldTier: lastTierRef.current, newTier: currentTier });
                setShowTierPopup(true);
                lastTierRef.current = currentTier;
                if (userEmail) {
                    localStorage.setItem(`previousTier_${userEmail}`, currentTier);
                }
            }
        };

        // Check periodically
        const interval = setInterval(checkTierChange, 2000);
        
        // Also check immediately when we know it might have changed
        window.addEventListener('tierUpdated', checkTierChange);

        return () => {
            clearInterval(interval);
            window.removeEventListener('tierUpdated', checkTierChange);
        };
    }, []);

    const handleCloseTierPopup = () => {
        setShowTierPopup(false);
    };

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
                         // -----------------------------
                // ADD TIER EXTRA TIME HERE
                // -----------------------------
                const userTier = localStorage.getItem('tier'); // Get user tier
                let extraMs = 0;
                if (userTier === 'SILVER') extraMs = 2 * 60 * 1000; // 2 min
                if (userTier === 'GOLD') extraMs = 5 * 60 * 1000;   // 5 min

                // Adjust expiry time with extra time
                const originalExpiry = new Date(response.data.expiresAt);
                const adjustedExpiry = new Date(originalExpiry.getTime() + extraMs);
                // -----------------------------


                    setActiveReservation({
                        hasActiveReservation: true,
                        bikeId,
                        stationId,
                        //expiresAt: response.data.expiresAt,
                        expiresAt: adjustedExpiry.toISOString(), // <-- use adjusted time
                        reservationId: response.data.reservationId
                    });
                    setReservationSuccessPopup(true);
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
        localStorage.removeItem('tier'); // Clear tier on logout
        delete axios.defaults.headers.common['Authorization'];
        navigate('/login?logout=1', { replace: true });
    };

    const handleSwitchRole = async () => {



            const actualRole = localStorage.getItem('actual_user_role');

            // Only allow switching if the user is actually an OPERATOR in the database
            if (actualRole !== 'OPERATOR') {
                alert('Only operators can switch roles. You are a rider and cannot become an operator.');
                return;
            }

            // If user is an operator, allow switching between OPERATOR and RIDER
            const currentRole = localStorage.getItem('user_role');
            if (currentRole === 'RIDER') {
                localStorage.setItem('user_role', 'OPERATOR');
            } else {
                localStorage.setItem('user_role', 'RIDER');
            }
            navigate('/home', { replace: true });
    };


    const handleViewHistory = () => {
        navigate('/history');
    };

    const handleConfirmRental = async () => {
        setConfirmRental({ active: false, dock: null, bike: null, station: null });
        await withLoading('Processing your rental...', async () => {
            const bikeId = confirmRental.bike.bikeId;
            const dockId = confirmRental.dock.dockId;
            const stationId = confirmRental.station.stationId;
            try {
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
                const response = await axios.post("http://localhost:8080/api/trips/return", {
                    userEmail,
                    tripId,
                    dockId,
                    bikeId,
                    stationId
                });
                //set the updated tier in localStorage if returned from backend
                if (response.data.userTier) {
                    localStorage.setItem('tier', response.data.userTier);
                    console.log('Tier updated after trip finished:', response.data.userTier);
                    // Trigger custom event to notify other components
                    window.dispatchEvent(new Event('tierUpdated'));
                }

                // Update FlexMoney balance in localStorage if returned
                if (response.data.flexMoneyBalance !== undefined && response.data.flexMoneyBalance !== null) {
                    localStorage.setItem('flexMoney', response.data.flexMoneyBalance);
                    console.log('FlexMoney updated after trip finished:', response.data.flexMoneyBalance);
                    window.dispatchEvent(new Event('flexMoneyUpdated'));
                }

                // Store trip summary data
                setTripSummaryData(response.data);
                setReturnSuccessPopup(true);

                setActiveBikeRental({
                    hasOngoingRental: false,
                    bikeId: null,
                    tripId: null,
                    dock: null,
                    station: null
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

    const handleBikeMaintain = async (bike, dockId, stationId) => {
        await withLoading('Updating bike maintenance status...', async () => {
            try {
                await axios.post('http://localhost:8080/api/operator/maintenance/set', { bikeId: bike.bikeId, dockId: dockId, stationId: stationId });

                // Update bikesUnderMaintenance
                setBikesUnderMaintenance(prev => {
                    const isAlreadyInList = prev.some(b => b.bikeId === bike.bikeId);
                    if (!isAlreadyInList) {
                        return [...prev, { bikeId: bike.bikeId, stationId }];
                    }
                    return prev;
                });

                await fetchStations();
            } catch (error) {
                console.error("Error updating bike maintenance status:", error);
                alert(`Failed to update bike maintenance status: ${error.response?.data || error.message}`);
            }
        });
    };

    const handleRemoveFromMaintenance = async (bikeId, dockId, stationId) => {
        await withLoading('Removing bike from maintenance...', async () => {
            try {
                await axios.post('http://localhost:8080/api/operator/maintenance/remove', { bikeId, dockId, stationId });

                // Update bikesUnderMaintenance
                setBikesUnderMaintenance(prev => prev.filter(bike => bike.bikeId !== bikeId));

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
        tripSummaryData,
        bikesUnderMaintenance,
        activeBikeMaintenanceRemoval,
        // Popups & control
        confirmRental,
        rentalSuccessPopup,
        confirmReturn,
        returnSuccessPopup,
        confirmReservation,
        reservationSuccessPopup,
        showCancelReservationPopup,
        reservationExpiredPopup,
        showTierPopup,
        tierChangeData,
        handleCloseTierPopup,

        // Actions
        handleLogout,
        handleSwitchRole,
        handleViewHistory,
        fetchStations,
        onClickShowConfirmRental: (dock, bike, station) => setConfirmRental({ active: true, dock, bike, station }),
        onClickShowConfirmReturn,
        handleShowReservation,
        onClickShowCancelReservation: () => setShowCancelReservationPopup(true),
        toggleStationStatus,
        rebalanceBike,
        handleConfirmReservation,
        setConfirmReservation,
        setReservationSuccessPopup,
        setReservationExpiredPopup,
        handleCancelActiveReservation,
        setShowCancelReservationPopup,
        handleConfirmRental,
        handleCancelConfirmationRental,
        handleCancelEventRental,
        handleConfirmReturn,
        handleCancelConfirmationReturn,
        handleCancelEventReturn,
        handleBikeMaintain,
        handleRemoveFromMaintenance,
        setActiveBikeMaintenanceRemoval,
        operatorEvents,
    };
}