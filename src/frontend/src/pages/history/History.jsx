import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import NavigationBar from "../../components/navigationBar/NavigationBar";
import "../home/Home.css";
import "./History.css";

const History = () => {
  const navigate = useNavigate();
  const [allTrips, setAllTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchId, setSearchId] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [bikeType, setBikeType] = useState("");
  const [toDate, setToDate] = useState("");
  const [expandedTrips, setExpandedTrips] = useState({});
  const [selectedBill, setSelectedBill] = useState(null);
  const [isConnected, setIsConnected] = useState(false);

  const userRole = localStorage.getItem("user_role");
  const MAX_RETRIES = 5;
  const RETRY_DELAY = 2000;
  const [sseRetryCount, setSseRetryCount] = useState(0);

  // SSE connection for real-time trip updates
  useEffect(() => {
    const eventSource = new EventSource(
      "http://localhost:8080/api/stations/subscribe",
      { withCredentials: true }
    );

    eventSource.onopen = () => {
      console.log("Trip History SSE connection established");
      setIsConnected(true);
      setSseRetryCount(0);
    };

    eventSource.onerror = (err) => {
      console.error("Trip History SSE connection error:", err);
    };

    eventSource.addEventListener("trip-update", (event) => {
      try {
        console.log("Received trip-update event:", event);
        const tripData = JSON.parse(event.data);
        console.log("Parsed tripData:", tripData);

        // filter by user (only concerned rider and all operators should receive real-time updates of the new trip)
        const currentUserId = localStorage.getItem("user_id");
        if (
          userRole !== "OPERATOR" &&
          tripData.userId?.toString() !== currentUserId
        ) {
          console.log("Ignoring trip update for other rider.", tripData.userId);
          return;
        }

        setAllTrips((prevTrips) => {
          const i = prevTrips.findIndex((t) => t.tripId === tripData.tripId);
          if (i !== -1) {
            const updated = [...prevTrips];
            updated[i] = { ...updated[i], ...tripData };
            return updated;
          } else {
            return [tripData, ...prevTrips];
          }
        });
      } catch (error) {
        console.error("Error parsing trip update SSE data:", error);
      }
    });
    return () => {
      console.log("Cleaning up Trip History SSE connection");
      eventSource.close();
    };
  }, []);

  //Load all trips on page load (all users if operator, only user's trips if not)
  useEffect(() => {
    loadTrips();
  }, []);

  const loadTrips = async () => {
    setLoading(true);
    try {
      let response;

      // If operator, fetch all trips, otherwise fetch only user's trips
      if (userRole === "OPERATOR") {
        response = await axios.post(
          "http://localhost:8080/api/history/AllTripsOperator"
        );
      } else {
        response = await axios.post(
          "http://localhost:8080/api/history/AllTrips",
          {
            userEmail: localStorage.getItem("user_email"),
          }
        );
      }

      // Sort trips by end time (most recent first)
      const sortedTrips = response.data.sort((a, b) => {
        if (!a.endTime) return 1;
        if (!b.endTime) return -1;
        return new Date(b.endTime) - new Date(a.endTime);
      });

      setAllTrips(sortedTrips);
    } catch (err) {
      if (err.response?.status !== 404) {
        alert("Failed to load trips");
      }
    } finally {
      setLoading(false);
    }
  };

  const filterTrips = () => {
    // Validate trip ID as it should only be numbers
    if (searchId && !/^\d+$/.test(searchId)) {
      alert("Trip ID must contain only numbers");
      setSearchId("");
      return allTrips; // Return all trips instead of empty array
    }

    // Validate date range - all possible combinations
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Reset to start of day for comparison

    // Case 1: Both dates are provided
    if (fromDate && toDate) {
      const from = new Date(fromDate);
      const to = new Date(toDate);

      // Check if from date is after to date
      if (from > to) {
        alert("Starting date cannot be later than finishing date");
        setFromDate("");
        setToDate("");
        return allTrips; // Return all trips
      }

      // Check if from date is in the future
      if (from > today) {
        alert("Starting date cannot be in the future");
        setFromDate("");
        return allTrips; // Return all trips
      }

      // Check if to date is in the future
      if (to > today) {
        alert("Finishing date cannot be in the future");
        setToDate("");
        return allTrips; // Return all trips
      }
    }
    // Case 2: Only from date is provided
    else if (fromDate && !toDate) {
      const from = new Date(fromDate);

      // Check if from date is in the future
      if (from > today) {
        alert("Starting date cannot be in the future");
        setFromDate("");
        return allTrips; // Return all trips
      }
    }
    // Case 3: Only to date is provided
    else if (!fromDate && toDate) {
      const to = new Date(toDate);

      // Check if to date is in the future
      if (to > today) {
        alert("Finishing date cannot be in the future");
        setToDate("");
        return allTrips; // Return all trips instead of empty array
      }
    }

    // If no filters are applied, return all trips
    if (!searchId && !fromDate && !toDate && !bikeType) {
      return allTrips;
    }

    // Apply filters only when at least one filter is active
    return allTrips.filter((trip) => {
      // Filter by ID
      if (searchId && !trip.tripId.toString().includes(searchId)) {
        return false;
      }

      // Filter by date
      if (fromDate || toDate) {
        if (!trip.startTime) return false;
        const tripDate = new Date(trip.startTime);

        if (fromDate && tripDate < new Date(fromDate)) return false;
        if (toDate && tripDate > new Date(toDate + "T23:59:59")) return false;
      }

      //filter by bike type
      if (bikeType && trip.bikeType !== bikeType) {
        return false;
      }

      return true;
    });
  };

  const toggleTripDetails = (tripId) => {
    setExpandedTrips((prev) => ({
      ...prev,
      [tripId]: !prev[tripId],
    }));
  };

  const handleViewBill = (trip) => {
    setSelectedBill({
      billId: trip.billId,
      baseFare: trip.baseFare,
      perMinuteRate: trip.perMinuteRate,
      totalAmount: trip.billCost,
      duration: (
        (new Date(trip.endTime) - new Date(trip.startTime)) /
        60000
      ).toFixed(2),
    });
  };

  const closeBillPopup = () => {
    setSelectedBill(null);
  };

  const filtered = filterTrips();

  return (
    <div className="history-container">
      <h1>Trip History</h1>
      <button onClick={() => navigate("/home")}>Back to Home</button>

      {/* Filters */}
      <div className="filters-section">
        <h3>Search</h3>

        <div className="filter-group">
          <label>Trip ID: </label>
          <input
            type="text"
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
            placeholder="Enter ID"
          />
        </div>

        <div className="filter-group date-input-group">
          <label>From: </label>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
          />
          <label>To: </label>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <label>Bike Type: </label>
          <select
            value={bikeType}
            onChange={(e) => setBikeType(e.target.value)}
          >
            <option value="">All</option>
            <option value="STANDARD">STANDARD</option>
            <option value="E_BIKE">E_BIKE</option>
          </select>
        </div>

        <button
          onClick={() => {
            setSearchId("");
            setFromDate("");
            setToDate("");
            setBikeType("");
          }}
        >
          Clear
        </button>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : filtered.length === 0 ? (
        <p>
          {allTrips.length === 0 ? "No trips found" : "No trips match filters"}
        </p>
      ) : (
        <div>
          <p>
            {filtered.length} trip(s){" "}
            {allTrips.length !== filtered.length && `of ${allTrips.length}`}
          </p>
          {filtered.map((trip) => (
            <div key={trip.tripId} className="trip-card">
              <h3>Trip #{trip.tripId}</h3>
              <p>User ID: {trip.userId}</p>
              <p>Username: {trip.userName}</p>
              <p>From Station: {trip.startStationId || "N/A"}</p>
              <p>To Station: {trip.endStationId || "N/A"}</p>
              <p>Bike Type: {trip.bikeType}</p>

              {expandedTrips[trip.tripId] && (
                <div className="trip-details-expanded">
                  <h4> Detailed view </h4>
                  <p>Bike ID: {trip.bikeId || "N/A"}</p>
                  <p>Start Time: {trip.startTime || "N/A"}</p>
                  <p>End Time: {trip.endTime || "N/A"}</p>
                  <p>
                    Duration:{" "}
                    {(
                      (new Date(trip.endTime) - new Date(trip.startTime)) /
                      60000
                    ).toFixed(2)}{" "}
                    minutes
                  </p>
                  <p>Status: {trip.status || "N/A"}</p>
                  <p>Bill ID: {trip.billId || "N/A"}</p>
                  <div className="trip-billing-info">
                    <div className="billing-details">
                      <p>
                        Bill Cost: $
                        {trip.billCost ? trip.billCost.toFixed(2) : "N/A"}
                      </p>
                    </div>
                    {trip.billId && (
                      <button
                        className="view-bill-btn"
                        onClick={() => handleViewBill(trip)}
                      >
                        View Charge breakdown
                      </button>
                    )}
                  </div>
                </div>
              )}

              <button onClick={() => toggleTripDetails(trip.tripId)}>
                {expandedTrips[trip.tripId] ? "View Less" : "View More"}
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Bill Details Popup */}
      {selectedBill && (
        <div className="bill-popup-overlay">
          <div className="bill-popup-content">
            <h3>Bill Details</h3>
            <div className="bill-info">
              <p>
                <strong>Bill ID:</strong> {selectedBill.billId}
              </p>
              <p>
                <strong>Duration:</strong> {selectedBill.duration} minutes
              </p>
            </div>

            <hr />

            <h4>Cost Breakdown</h4>
            <div className="cost-breakdown">
              <div className="breakdown-item">
                <span>Base Fare:</span>
                <span>${selectedBill.baseFare.toFixed(2)}</span>
              </div>
              <div className="breakdown-item">
                <span>Time Charge:</span>
                <span>
                  ${selectedBill.perMinuteRate.toFixed(2)} ×{" "}
                  {selectedBill.duration} min
                </span>
              </div>
              <div className="breakdown-item">
                <span></span>
                <span>
                  $
                  {(selectedBill.perMinuteRate * selectedBill.duration).toFixed(
                    2
                  )}
                </span>
              </div>
              <hr />
              <div className="breakdown-total">
                <span>
                  <strong>Total Amount:</strong>
                </span>
                <span>
                  <strong>${selectedBill.totalAmount.toFixed(2)}</strong>
                </span>
              </div>
            </div>

            <button onClick={closeBillPopup} className="close-popup-btn">
              &times;
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default History;
