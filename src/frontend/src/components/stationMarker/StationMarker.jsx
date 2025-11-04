import { Marker, Popup } from "react-leaflet";
import React, { useState, useEffect } from 'react';
import "./StationMarker.css"

//added onClickShowConfirmReservation
function StationMarker({
    station,
    onClickShowConfirmRental,
    activeBikeRental,
    onClickShowConfirmReturn,
    onClickShowConfirmReservation,
    activeReservation,
    onClickShowCancelReservation,
    toggleStationStatus,
    userRole,
    rebalanceSource,
    handleRebalanceSource,
    handleRebalanceTarget
}) {
    // State to track the current selected dock
    const [selectedDock, setSelectedDock] = useState(null);

    // Update selectedDock when its data changes in the station
    useEffect(() => {
        if (!selectedDock) return;
        
        // Find the updated dock data
        const updatedDock = station.docks.find(dock => dock.dockId === selectedDock.dockId);
        if (updatedDock) {
            setSelectedDock(updatedDock);
        }
    }, [station, selectedDock?.dockId]);

    // gets bike and source dock/station for rebalancing, retrieve button
    const handleRetrieve = (dock) => {
        if (!dock.bike) return;
        handleRebalanceSource(dock.bike, dock, station.stationId);
    };

    // gets target dock/station for rebalancing, rebalance button
    const handleRebalance = (targetDock) => {
        handleRebalanceTarget(targetDock, station.stationId);
        setSelectedDock(null);
    };
    
    return (
        <Marker
        key={station.stationId}
        position={station.position.split(",").map((coord) => parseFloat(coord.trim()))}
        >
            <Popup>
                <div className="flex flex-col min-w-[220px]">
                    <h4 className="mb-2 font-semibold">{station.stationName}</h4>

                    <p style={{ margin: "0.3em" }}>Status: {station.stationStatus}</p>

                    {/* operator only button */}
                    {userRole === "OPERATOR" && (
                        <button
                            className="button-operator-toggle"
                            onClick={() => toggleStationStatus(station.stationId, station.stationStatus)}>
                            {station.stationStatus === "ACTIVE" ? "Set OUT_OF_SERVICE" : "Set ACTIVE"}
                        </button>
                    )}


                    <div className="flex flex-row flex-wrap gap-2 mb-2" style={{ display: 'flex', flexDirection: 'row' }}>
                        {station.docks.map((dock) => {
                            const hasBike = dock.bike !== null;
                            const isReserved = dock.bike?.status === "RESERVED"; // or BikeStatus.RESERVED if you have enum mapping

                            return (
                                // Small boxes to represent the bikes in a station
                                <div
                                key={dock.dockId}

                                // Show bike ID on hover (with 'reserved' next to it if its reserved)
                                title={
                                    hasBike
                                    ? `Bike ID: ${dock.bike.bikeId}${isReserved ? " (Reserved)" : ""}`
                                    : "Empty Dock"
                                }

                                // Style the boxes that represent the bikes
                                className={`bike-box ${
                                    hasBike
                                        ? isReserved
                                            ? "bike-box-reserved"
                                            : "bike-box-available"
                                        : "bike-box-empty"
                                } ${selectedDock?.dockId === dock.dockId ? "bike-box-selected" : ""}`}
                                    
                                onClick={() => setSelectedDock(dock)}
                            >
                            {hasBike ? <span style={{ fontSize: "1.2em" }}>🚲</span> : ""}
                            </div>
                        );
                        })}
                    </div>
                    
                    {/* Display the dock info on selection */}
                    {selectedDock && (
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <div>
                                <h4>Dock {selectedDock.dockId}</h4>
                                <p style={{ margin: "0.3em" }}>
                                Bike ID: {selectedDock.bike?.bikeId || "None"}
                                <br />
                                Bike Status: {selectedDock.bike?.status || "EMPTY"}
                                </p>
                            </div>
                            

                            {/* Rent button */}
                            {selectedDock.bike && !activeBikeRental.hasOngoingRental && userRole !== "OPERATOR" && (
                            // Allow rent only if:
                            // - The bike is not reserved
                            // - OR the bike is the one the user reserved
                            (!activeReservation.hasActiveReservation || activeReservation.bikeId === selectedDock.bike.bikeId) &&
                            (selectedDock.bike.status !== "RESERVED" || activeReservation?.bikeId === selectedDock.bike.bikeId) && (
                                <button
                                className="button-19"
                                onClick={() => onClickShowConfirmRental(selectedDock, selectedDock.bike, station)}
                                >
                                Rent This Bike
                                </button>
                            )
                            )}

                            {/* Return button */}
                            { activeBikeRental.hasOngoingRental && 
                            selectedDock.dockStatus === "EMPTY" && 
                            userRole !== "OPERATOR" &&
                            station.stationStatus === "ACTIVE" && (
                                <button
                                className="button-19-return"
                                onClick={() => onClickShowConfirmReturn(selectedDock, activeBikeRental.bikeId, station)}
                                >
                                    Return Your Bike
                                </button>
                            )}

                            {/* Reserve / Cancel button */}
                            {selectedDock.bike && userRole !== "OPERATOR" && !activeBikeRental.hasOngoingRental &&
                            (selectedDock.bike.status !== "RESERVED" || activeReservation?.bikeId === selectedDock.bike.bikeId) && (
                            <>
                                {/* If the user has no active reservation → show Reserve button */}
                                {!activeReservation?.hasActiveReservation && (
                                <button
                                    className="button-19-reserve"
                                    onClick={() => onClickShowConfirmReservation(selectedDock.bike, station)}
                                >
                                    Reserve This Bike
                                </button>
                                )}

                                {/* If the user has an active reservation on THIS bike → show Cancel button */}
                                {activeReservation?.hasActiveReservation &&
                                activeReservation.bikeId === selectedDock.bike.bikeId && (
                                <button
                                    className="button-19-cancel"
                                    onClick={() => onClickShowCancelReservation(selectedDock.bike, station)}
                                >
                                    Cancel Reservation
                                </button>
                                )}

                                {/* If the user has an active reservation on another bike → no button */}
                            </>
                            )}



                            {/* operator retrieve source bike button */}
                            {userRole === "OPERATOR" && 
                             selectedDock.bike && 
                             !rebalanceSource.bikeId && (
                                <button
                                    className="button-19"
                                    onClick={() => handleRetrieve(selectedDock)}
                                >
                                    Retrieve This Bike
                                </button>
                            )}

                            {/* operator rebalance bike button */}
                            {userRole === "OPERATOR" && 
                             rebalanceSource.bikeId &&
                             selectedDock.dockId !== rebalanceSource.sourceDockId && 
                             !selectedDock.bike && (
                                <button
                                    className="button-19-return"
                                    onClick={() => handleRebalance(selectedDock)}
                                >
                                    Rebalance Bike Here
                                </button>
                            )}

                            {/* Close button */}
                            <button
                            className="button-28" 
                            onClick={(e) => {
                                e.stopPropagation(); // Stops the leaflet 'Popup' from counting it as an external click, i.e. stops the entire popup from closing
                                setSelectedDock(null);
                            }}
                            >
                            Close
                            </button>
                        </div>
                    )}
                </div>
            </Popup>
        </Marker>
    );
}

export default StationMarker;
