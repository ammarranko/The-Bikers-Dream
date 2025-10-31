import { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Map from '../../components/Map'
import ConfirmationPopup from "../../components/confirmationPopup/ConfirmationPopup";
import EventPopup from "../../components/eventPopup/EventPopup"

const Home = () => {
    const navigate = useNavigate();

    const token = localStorage.getItem('jwt_token');
    if (token && token !== "null") {
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
        delete axios.defaults.headers.common['Authorization'];
    }

    // State to track reservation
    const [confirmReservation, setConfirmReservation] = useState({
        active: false,
        bike: null,
        station: null
    });
     
    //return success 
    const [reservationSuccessPopup, setReservationSuccessPopup] = useState(false);

    // State to track whether the user wants to confirm and associated variables
    const [confirmRental, setConfirmRental] = useState({
        active: false,
        dock: null,
        bike: null,
        station: null
    });

    // State to track whether the user wants to confirm
    const [confirmReturn, setConfirmReturn] = useState({
        active: false,
        dock: null,
        bike: null,
        station: null
    });

    // State to track the event popup window for returns
    const [returnSuccessPopup, setReturnSuccessPopup] = useState(false);

    // State to track the event popup window for rentals
    const [rentalSuccessPopup, setRentalSuccessPopup] = useState(false);

    // State to track if the current user has an active bike rental and details as well (bikeId, userId, if it exists)
    // Default empty object to help with consistency
    const [activeBikeRental, setActiveBikeRental] = useState({
        hasOngoingRental: false,
        bikeId: null,
        tripId: null,
        dock: null,
        station: null
    });
        

// Track if user has an active reservation
const [activeReservation, setActiveReservation] = useState({
    hasActiveReservation: false,
    bikeId: null,
    stationId: null,
    expiresAt: null,
    reservationId: null
});

    // State to hold stations data
    const [stations, setStations] = useState([]);
 
    // Retrieve full name from localStorage
    const fullName = localStorage.getItem('user_full_name');
    const role = localStorage.getItem('user_role');
    let userEmail =  localStorage.getItem('user_email');

    // Fetch all stations on startup
    // Check if the user has active bike rentals on startup
    useEffect(() => {
        fetchStations();
        fetchActiveRental();
        fetchActiveReservation(); // added reservation
    }, []);




    // Track time left until reservation expires
const [timeLeft, setTimeLeft] = useState(null);

// Update the countdown every second
useEffect(() => {
  if (!activeReservation?.expiresAt) {
    setTimeLeft(null);
    return;
  }

  // operator can toggle station status
    const toggleStationStatus = async (stationId, currentStatus) => {
        const newStatus = currentStatus === "ACTIVE" ? "OUT_OF_SERVICE" : "ACTIVE";

        try {
            await axios.post('http://localhost:8080/api/operator/stations/status', { 
                stationId: stationId,
                status: newStatus
            });
            fetchStations();
        } catch (error) {
            console.error("Error changing station status:", error);
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            } else {
                alert(`Failed updating station status: ${error.response?.data || error.message}`);
            }
        }
    };

    // operator can rebalance one bike
    const rebalanceBike = async (rebalanceData) => {
        try {
            await axios.post(`http://localhost:8080/api/operator/rebalance`, rebalanceData);
            fetchStations();
        } catch (error) {
            console.error("Error rebalancing bike:", error);
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            } else {
                alert(`Failed rebalancing bike: ${error.response?.data || error.message}`);
            }
        }
    };

  /*
        --- Rental Confirmation Logic ---
    */
  const onClickShowConfirmRental = (dock, bike, station) => {
    setConfirmRental({
      active: true,
      dock: dock,
      bike: bike,
      station: station,
    });
  };
  const interval = setInterval(() => {
    const now = new Date();
    const expiry = new Date(activeReservation.expiresAt);
    const diffMs = expiry - now;

    if (diffMs <= 0) {
      clearInterval(interval);
      setTimeLeft("Expired");
      fetchActiveReservation(); // refresh state automatically
    } else {
      const minutes = Math.floor(diffMs / 60000);
      const seconds = Math.floor((diffMs % 60000) / 1000);
      setTimeLeft(`${minutes}m ${seconds}s`);
    }
  }, 1000);

  return () => clearInterval(interval);
}, [activeReservation]);

// Function to check if user has an active reservation
const fetchActiveReservation = async () => {
    try {
        const response = await axios.post("http://localhost:8080/api/reservations/check", { userEmail });
        console.log("Received check reservation response:", response.data);

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
    }
};

    // Function to fetch an active rental if it exists for a user
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

    // Function to fetch all stations from the db, also for refreshing
    async function fetchStations() {
            try {
                const response = await axios.get("http://localhost:8080/api/stations/allStations/details");
                console.log("Received initial response with data: ", response.data)
                setStations(response.data);
            } catch (error) {
                console.error("Error fetching station:", error);
            }
        }

    // Function to check for active bike rentals
    const checkRental = async () => {
        try {
            const response = await axios.post("http://localhost:8080/api/trips/checkRental", { userEmail });
            console.log("Received check rental response with data: ", response.data)

            if(response.data.hasOngoingRental) {
                return {
                    hasOngoingRental: true,
                    bikeId: response.data.bikeId,
                    tripId: response.data.tripId,
                    dock: response.data.dock,
                    station: response.data.station
                };
            } else {
                return { hasOngoingRental: false, bikeId: null, tripId: null, dock: null, station: null };
            }
        } catch (error) {
            console.error("Full error object:", error);
            console.error("Error response:", error.response);
            console.error("Error status:", error.response?.status);
            console.error("Error data:", error.response?.data);
            
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            } else if (error.response?.status === 404) {
                alert("Bike rental unable to be checked. Resource not found. Please check the bike/dock availability.");
            } else {
                alert(`Failed to rent bike: ${error.response?.data || error.message}`);
            }

            return { hasOngoingRental: false, bikeId: null, tripId: null, dock: null, station: null };
        }
    }

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
        --- Reservation: called when the rider clicks the “Reserve” button ---
    */
    const handleShowReservation = (bike, station) => {
        setConfirmReservation({
                active: true,
                bike,
                station
            });
        };
    /*
        --- Reservation: function to send a request to backend
    */ 
    const handleConfirmReservation = async () => {
            const bikeId = confirmReservation.bike.bikeId;
            const stationId = confirmReservation.station.stationId;
            const userEmail = localStorage.getItem('user_email'); 
        
            try {
              const response = await axios.post("http://localhost:8080/api/reservations/create", {
                  bikeId,
                  stationId,
                  userEmail
              });

              console.log("Reservation created response:", response.data);


        if (response.data) {
          // Update frontend state with the new active reservation
          setActiveReservation({
            hasActiveReservation: true,
            bikeId,
            stationId,
            expiresAt: response.data.expiresAt, // backend should return expiration
            reservationId: response.data.reservationId
        });
            setReservationSuccessPopup(true); // Show the reservation success popup
              // 🔄 Refresh station data so the button color updates right away
            await fetchStations();  
              // ✅ Close confirmation popup
            setConfirmReservation({ active: false, bike: null, station: null });

        }
    } catch (error) {
        console.error("Reservation failed:", error.response?.data || error.message);
        alert("Failed to create reservation. Please try again.");
    }
};
    

    /*
        --- Reservation: handler to cancel the reservation popup
    */    
        const [showCancelReservationPopup, setShowCancelReservationPopup] = useState(false);

const handleCancelActiveReservation = async () => {
  console.log("Cancel clicked!", activeReservation);
  if (!activeReservation?.hasActiveReservation) return;

  const reservationId = Number(activeReservation.reservationId); // convert to number

  //const reservationId = activeReservation.reservationId;

  if (!reservationId) {
    alert("No reservation to cancel.");
    return;
  }

  try {
    const response = await axios.post(
      "http://localhost:8080/api/reservations/cancel",
      { reservationId }  // make sure this is a number
    );

    // Update frontend state
    setActiveReservation({
      hasActiveReservation: false,
      bikeId: null,
      stationId: null,
      expiresAt: null,
      reservationId: null
    });
    // Clear timer
    setTimeLeft(null);

    // Refresh station data immediately
    await fetchStations();

    alert("Reservation cancelled successfully.");
  } catch (error) {
    console.error("Cancel reservation failed:", error.response?.data || error.message);
    alert("Failed to cancel reservation. Please try again.");
  }
};

      
      // --- Periodically check if reservation has expired ---
useEffect(() => {
  if (!activeReservation?.expiresAt || !activeReservation.hasActiveReservation) {
    setTimeLeft(null);
    return;
  }

  const interval = setInterval(() => {
    const now = new Date();
    const expiry = new Date(activeReservation.expiresAt);
    const diffMs = expiry - now;

    if (diffMs <= 0) {
      clearInterval(interval);

      // Reset state automatically when reservation expires
      setActiveReservation({
        hasActiveReservation: false,
        bikeId: null,
        stationId: null,
        expiresAt: null,
        reservationId: null
      });
      setTimeLeft(null);

      fetchStations(); // refresh station availability
    } else {
      const minutes = Math.floor(diffMs / 60000);
      const seconds = Math.floor((diffMs % 60000) / 1000);
      setTimeLeft(`${minutes}m ${seconds}s`);
    }
  }, 1000);

  return () => clearInterval(interval);
}, [activeReservation]);
       
           
    /*
        --- Rental Confirmation Logic ---
    */
    const onClickShowConfirmRental = (dock, bike, station) => {
        setConfirmRental({
            active: true,
            dock: dock, 
            bike: bike,
            station: station
        });
    };

    const handleConfirmRental = async () => {
        let bikeId = confirmRental.bike.bikeId;
        let dockId = confirmRental.dock.dockId;
        let stationId = confirmRental.station.stationId;

        try {
            const response = await axios.post("http://localhost:8080/api/trips/rent", {
                bikeId,
                userEmail,
                dockId,
                stationId
            });
            console.log("Received rental response with data: ", response.data)

            let dock = confirmRental.dock;
            let station = confirmRental.station;
            let tripId = response.data.tripId;

            setConfirmRental({
                active: false,
                dock: null,
                bike: null,
                station: null
            });

            setRentalSuccessPopup(true);

            setActiveBikeRental({    
                hasOngoingRental: true,
                bikeId,
                tripId,
                dock,
                station
            });

        // ✅ Clear reservation if renting the reserved bike
        if (activeReservation.hasActiveReservation && activeReservation.bikeId === bikeId) {
            setActiveReservation({
                hasActiveReservation: false,
                bikeId: null,
                stationId: null,
                expiresAt: null,
                reservationId: null
            });
            setTimeLeft(null);
        }
            fetchStations();
        } catch (error) {
            console.error("Full error object:", error);
            console.error("Error response:", error.response);
            console.error("Error status:", error.response?.status);
            console.error("Error data:", error.response?.data);
            
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            } else if (error.response?.status === 404) {
                alert("Resource not found. Please check the bike/dock availability.");
            } else {
                alert(`Failed to rent bike: ${error.response?.data || error.message}`);
            }
        }
    };

    const handleCancelConfirmationRental = () => {
        setConfirmRental({
            active: false,
            dock: null,
            bike: null,
            station: null
        });
    };

    const handleCancelEventRental = () => {
        setRentalSuccessPopup(false);
    };


    /*
        --- Return Confirmation Logic ---
    */
   const onClickShowConfirmReturn = (dock, bike, station) => {
        setConfirmReturn({
            active: true,
            dock: dock, 
            bike: bike, 
            station: station
        });
    };

    const handleCancelConfirmationReturn = () => {
        setConfirmReturn({
            active: false,
            dock: null,
            bike: null,
            station: null
        });
    };

    const handleCancelEventReturn = () => {
        setReturnSuccessPopup(false);
    };

    const handleConfirmReturn = async () => {
        let tripId = activeBikeRental.tripId
        let bikeId = activeBikeRental.bikeId;
        let dockId = confirmReturn.dock.dockId;
        let stationId = confirmReturn.station.stationId;

        try {
            const response = await axios.post("http://localhost:8080/api/trips/return", {
                tripId,
                bikeId,
                userEmail,
                dockId,
                stationId
            });
            console.log("Received return bike response with data: ", response.data)

            setConfirmReturn({
                active: false,
                dock: null,
                bike: null,
                station: null
            });

            setReturnSuccessPopup(true);

            setActiveBikeRental({
                hasOngoingRental: false,
                bikeId: null,
                tripId: null,
                dock: null,
                station: null
            });


            fetchStations();
        } catch (error) {
            console.error("Full error object:", error);
            console.error("Error response:", error.response);
            console.error("Error status:", error.response?.status);
            console.error("Error data:", error.response?.data);
            
            if (error.response?.status === 401) {
                alert("Unauthorized. Please login again.");
                handleLogout();
            } else if (error.response?.status === 404) {
                alert("Resource not found. Please check the bike/dock availability.");
            } else {
                alert(`Failed to return bike: ${error.response?.data || error.message}`);
            }
        }
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
                   {/* 🚲 Active reservation banner */}
          {activeReservation.hasActiveReservation && (
              <h3 style={{ color: "orange", textAlign: "center", marginTop: "10px" }}>
    🚲        Active Reservation: Bike #{activeReservation.bikeId} at Station #{activeReservation.stationId} <br />
              ⏰ Expires in: <b>{timeLeft || "Calculating..."}</b>
              </h3>
          )}

                
                <Map onClickShowConfirmRental={onClickShowConfirmRental} activeBikeRental={activeBikeRental}
                    onClickShowConfirmReturn={onClickShowConfirmReturn} stations={stations} 
                    setStations={setStations} onClickShowConfirmReservation={handleShowReservation} 
                    activeReservation={activeReservation}   onClickShowCancelReservation={() => setShowCancelReservationPopup(true)}
                   // added reservation
                />
                                {/* --- Available Bikes for Reservation --- */}
                                <div style={{ marginTop: "20px" }}>
                    <h2>Available Bikes for Reservation:</h2>
                    {stations.map((station) => (
    <div key={station.stationId} style={{ marginBottom: "10px" }}>
        <h3>{station.stationName} (ID: {station.stationId})</h3>
        <div style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
            {station.bikes?.filter(bike => !bike.isRented).map((bike) => (
                <button
                    key={bike.bikeId}
                    onClick={() => handleShowReservation(bike, station)}
                    disabled={activeReservation.hasActiveReservation || activeBikeRental.hasOngoingRental}
                    style={{ padding: "8px 12px", cursor: "pointer" }}
                >
                    Reserve Bike #{bike.bikeId}
                </button>
            ))}
        </div>
    </div>
))}

                </div>
                {/* --- Popups --- */}
                {confirmRental.active && (
                    <ConfirmationPopup
                        message={`You are about to rent a bike! 🚲

                                    Station: ${confirmRental.station.stationName} (ID: ${confirmRental.station.stationId})
                                    Dock: ${confirmRental.dock.dockId}
                                    Bike: ${confirmRental.bike.bikeId}

                                    Do you want to proceed with this rental?`
                                }
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
                        message={`You are about to return a bike! 🔙🚲

                                    Station: ${confirmReturn.station.stationName} (ID: ${confirmReturn.station.stationId})
                                    Dock: ${confirmReturn.dock.dockId}
                                    Bike: ${activeBikeRental.bikeId}

                                    Do you want to proceed with this return?`
                                }
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

                {activeBikeRental.hasOngoingRental && (
                    <h3>YOU CURRENTLY HAVE A BIKE RENTED, YOU MUST RETURN THIS BIKE BEFORE RENTING ANOTHER.</h3>
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

            </main>

            
        </div>
    );
};

export default Home;
