import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const History = () => {
    const navigate = useNavigate();
    const [allTrips, setAllTrips] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchId, setSearchId] = useState('');
    const [fromDate, setFromDate] = useState('');
    const [bikeType, setBikeType] = useState('');
    const [toDate, setToDate] = useState('');
    const [expandedTrips, setExpandedTrips] = useState({});

    const username = localStorage.getItem('username');

    //Load all trips of the logged user on page load
    useEffect(() => {
        loadTrips();
    }, []);

    const loadTrips = async () => {
        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8080/api/history/AllTrips', {
                userEmail: localStorage.getItem('user_email')
            });

            // Sort trips by end time (most recent first)
            const sortedTrips = response.data.sort((a, b) => {
                if (!a.endTime) return 1;
                if (!b.endTime) return -1;
                return new Date(b.endTime) - new Date(a.endTime);
            });

            setAllTrips(sortedTrips);
        } catch (err) {
            if (err.response?.status !== 404) {
                alert('Failed to load trips');
            }
        } finally {
            setLoading(false);
        }
    };

    const filterTrips = () => {
        // Validate trip ID as it should only be numbers
        if (searchId && !/^\d+$/.test(searchId)) {
            alert('Trip ID must contain only numbers');
            return [];
        }

        // Validate date range
        if (fromDate && toDate && fromDate > toDate) {
            alert("Starting date cannot be later than finishing date");
            return [];
        }

        return allTrips.filter(trip => {
            // Filter by ID
            if (searchId && !trip.tripId.toString().includes(searchId)) {
                return false;
            }

            // Filter by date
            if (fromDate || toDate) {
                if (!trip.startTime) return false;
                const tripDate = new Date(trip.startTime);

                if (fromDate && tripDate < new Date(fromDate)) return false;
                if (toDate && tripDate > new Date(toDate + 'T23:59:59')) return false;
            }

            //filter by bike type
            if (bikeType && trip.bikeType !== bikeType) {
                return false;
            }


            return true;
        });
    };

    const toggleTripDetails = (tripId) => {
        setExpandedTrips(prev => ({
            ...prev,
            [tripId]: !prev[tripId]
        }));
    };

    const filtered = filterTrips();

    return (
        <div style={{padding: '20px', maxWidth: '1000px'}}>
            <h1>Trip History</h1>
            <button onClick={() => navigate('/home')}>Back to Home</button>

            {/* Filters */}
            <div style={{margin: '20px 0', padding: '15px', border: '1px solid #ddd'}}>
                <h3>Search</h3>

                <div style={{marginBottom: '10px'}}>
                    <label>Trip ID: </label>
                    <input
                        type="text"
                        value={searchId}
                        onChange={(e) => setSearchId(e.target.value)}
                        placeholder="Enter ID"
                    />
                </div>

                <div style={{marginBottom: '10px'}}>
                    <label>From: </label>
                    <input
                        type="date"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                    />
                    <label style={{marginLeft: '10px'}}>To: </label>
                    <input
                        type="date"
                        value={toDate}
                        onChange={(e) => setToDate(e.target.value)}
                    />
                </div>

                <div style={{marginBottom: '10px'}}>
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

                <button onClick={() => { setSearchId(''); setFromDate(''); setToDate(''); }}>
                    Clear
                </button>
            </div>

            <h2>Trips for {localStorage.getItem('user_full_name')}</h2>

            {loading ? (
                <p>Loading...</p>
            ) : filtered.length === 0 ? (
                <p>{allTrips.length === 0 ? 'No trips found' : 'No trips match filters'}</p>
            ) : (
                <div>
                    <p>{filtered.length} trip(s) {allTrips.length !== filtered.length && `of ${allTrips.length}`}</p>
                    {filtered.map((trip) => (
                        <div key={trip.tripId} style={{margin: '10px 0', padding: '10px', border: '1px solid #ccc'}}>
                            <h3>Trip #{trip.tripId}</h3>
                            <p>User: {username}</p>
                            <p>From Station: {trip.startStationId || 'N/A'}</p>
                            <p>To Station: {trip.endStationId || 'N/A'}</p>
                            <p>Bike Type: {trip.bikeType}</p>

                            {expandedTrips[trip.tripId] && (
                                <div style={{color: 'red'}}>
                                    <h4> Detailed view </h4>
                                    <p>Start Time: {trip.startTime || 'N/A'}</p>
                                    <p>End Time: {trip.endTime || 'N/A'}</p>
                                    <p>  Duration: {((new Date(trip.endTime) - new Date(trip.startTime)) / 60000).toFixed(10)} minutes</p>
                                    <p>Status: {trip.status || 'N/A'}</p>
                                    <p>Bill ID: {trip.billId || 'N/A'}</p>
                                </div>
                            )}

                            <button onClick={() => toggleTripDetails(trip.tripId)}>
                                {expandedTrips[trip.tripId] ? 'View Less details' : 'Select trip'}
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default History;
