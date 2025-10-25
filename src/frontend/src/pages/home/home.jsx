import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Map from "../../components/Map";
import ConfirmationPopup from "../../components/confirmationPopup/ConfirmationPopup";
import EventPopup from "../../components/eventPopup/EventPopup";

const Home = () => {
  const navigate = useNavigate();

  const token = localStorage.getItem("jwt_token");
  if (token && token !== "null") {
    axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete axios.defaults.headers.common["Authorization"];
  }

  // State to track whether the user wants to confirm and associated variables
  const [confirmRental, setConfirmRental] = useState({
    active: false,
    dock: null,
    bike: null,
    station: null,
  });

  // State to track whether the user wants to confirm
  const [confirmReturn, setConfirmReturn] = useState({
    active: false,
    dock: null,
    bike: null,
    station: null,
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
    station: null,
  });

  // State to hold stations data
  const [stations, setStations] = useState([]);

  // Retrieve full name from localStorage
  const fullName = localStorage.getItem("user_full_name");
  const role = localStorage.getItem("user_role");
  let userEmail = localStorage.getItem("user_email");

  // Fetch all stations on startup
  // Check if the user has active bike rentals on startup
  useEffect(() => {
    fetchStations();
    fetchActiveRental();
  }, []);

  // Function to fetch an active rental if it exists for a user
  const fetchActiveRental = async () => {
    const responseData = await checkRental();
    setActiveBikeRental({
      hasOngoingRental: responseData.hasOngoingRental,
      bikeId: responseData.bikeId,
      tripId: responseData.tripId,
      dock: responseData.dock,
      station: responseData.station,
    });
  };

  // Function to fetch all stations from the db, also for refreshing
  async function fetchStations() {
    try {
      const response = await axios.get(
        "http://localhost:8080/api/stations/allStations/details"
      );
      console.log("Received initial response with data: ", response.data);
      setStations(response.data);
    } catch (error) {
      console.error("Error fetching station:", error);
    }
  }

  // Function to check for active bike rentals
  const checkRental = async () => {
    try {
      const response = await axios.post(
        "http://localhost:8080/api/trips/checkRental",
        { userEmail }
      );
      console.log("Received check rental response with data: ", response.data);

      if (response.data.hasOngoingRental) {
        return {
          hasOngoingRental: true,
          bikeId: response.data.bikeId,
          tripId: response.data.tripId,
          dock: response.data.dock,
          station: response.data.station,
        };
      } else {
        return {
          hasOngoingRental: false,
          bikeId: null,
          tripId: null,
          dock: null,
          station: null,
        };
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
        alert(
          "Bike rental unable to be checked. Resource not found. Please check the bike/dock availability."
        );
      } else {
        alert(`Failed to rent bike: ${error.response?.data || error.message}`);
      }

      return {
        hasOngoingRental: false,
        bikeId: null,
        tripId: null,
        dock: null,
        station: null,
      };
    }
  };

  const handleLogout = () => {
    try {
      localStorage.removeItem("jwt_token");
      localStorage.removeItem("user_email");
      localStorage.removeItem("user_full_name");
      localStorage.removeItem("user_role");
      delete axios.defaults.headers.common["Authorization"];
    } finally {
      // Navigate to login page and trigger auth logout handling
      navigate("/login?logout=1", { replace: true });
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

  const handleConfirmRental = async () => {
    let bikeId = confirmRental.bike.bikeId;
    let dockId = confirmRental.dock.dockId;
    let stationId = confirmRental.station.stationId;

    try {
      const response = await axios.post(
        "http://localhost:8080/api/trips/rent",
        {
          bikeId,
          userEmail,
          dockId,
          stationId,
        }
      );
      console.log("Received rental response with data: ", response.data);

      let dock = confirmRental.dock;
      let station = confirmRental.station;
      let tripId = response.data.tripId;

      setConfirmRental({
        active: false,
        dock: null,
        bike: null,
        station: null,
      });

      setRentalSuccessPopup(true);

      setActiveBikeRental({
        hasOngoingRental: true,
        bikeId,
        tripId,
        dock,
        station,
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
        alert(`Failed to rent bike: ${error.response?.data || error.message}`);
      }
    }
  };

  const handleCancelConfirmationRental = () => {
    setConfirmRental({
      active: false,
      dock: null,
      bike: null,
      station: null,
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
      station: station,
    });
  };

  const handleCancelConfirmationReturn = () => {
    setConfirmReturn({
      active: false,
      dock: null,
      bike: null,
      station: null,
    });
  };

  const handleCancelEventReturn = () => {
    setReturnSuccessPopup(false);
  };

  const handleConfirmReturn = async () => {
    let tripId = activeBikeRental.tripId;
    let bikeId = activeBikeRental.bikeId;
    let dockId = confirmReturn.dock.dockId;
    let stationId = confirmReturn.station.stationId;

    try {
      const response = await axios.post(
        "http://localhost:8080/api/trips/return",
        {
          tripId,
          bikeId,
          userEmail,
          dockId,
          stationId,
        }
      );
      console.log("Received return bike response with data: ", response.data);

      setConfirmReturn({
        active: false,
        dock: null,
        bike: null,
        station: null,
      });

      setReturnSuccessPopup(true);

      setActiveBikeRental({
        hasOngoingRental: false,
        bikeId: null,
        tripId: null,
        dock: null,
        station: null,
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
        alert(
          `Failed to return bike: ${error.response?.data || error.message}`
        );
      }
    }
  };

  useEffect(() => {
    const eventSource = new EventSource(
      "http://localhost:8080/api/stations/stream"
    );
    eventSource.addEventListener("station-update", (event) => {
      const updatedStation = JSON.parse(event.data);
      setStations((prevStations) => {
        const index = prevStations.findIndex(
          (s) => s.stationId === updatedStation.stationId
        );
        if (index !== -1) {
          // Replace existing station
          const newStations = [...prevStations];
          newStations[index] = updatedStation;
          return newStations;
        } else {
          // Add new station
          return [...prevStations, updatedStation];
        }
      });
    });
    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div style={{ padding: "16px" }}>
      <header
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <h1>Hello{fullName ? `, ${fullName}` : ""}!</h1>
        <h1>Hello{role ? `, ${role}` : ""}!</h1>

        <button
          type="button"
          onClick={handleLogout}
          style={{ padding: "8px 12px", cursor: "pointer" }}
        >
          Logout
        </button>
      </header>

      <main>
        <p>Welcome to the app.</p>
        <Map
          onClickShowConfirmRental={onClickShowConfirmRental}
          activeBikeRental={activeBikeRental}
          onClickShowConfirmReturn={onClickShowConfirmReturn}
          stations={stations}
          setStations={setStations}
        />

        {confirmRental.active && (
          <ConfirmationPopup
            message={`You are about to rent a bike! 🚲

                                    Station: ${confirmRental.station.stationName} (ID: ${confirmRental.station.stationId})
                                    Dock: ${confirmRental.dock.dockId}
                                    Bike: ${confirmRental.bike.bikeId}

                                    Do you want to proceed with this rental?`}
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

                                    Do you want to proceed with this return?`}
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
          <h3>
            YOU CURRENTLY HAVE A BIKE RENTED, YOU MUST RETURN THIS BIKE BEFORE
            RENTING ANOTHER.
          </h3>
        )}
      </main>
    </div>
  );
};

export default Home;
