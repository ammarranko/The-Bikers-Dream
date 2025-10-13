import { Marker, Popup } from "react-leaflet";
import React from "react";

function StationMarker({ station }) {
  return (
    <Marker
    key={station.stationId}
    position={station.position.split(",").map((coord) => parseFloat(coord.trim()))}
    >
        <Popup>
            <div style={{ minWidth: "220px" }}>
                <h4 style={{ margin: "0 0 10px 0" }}>{station.stationName}</h4>

                <div style={{ display: "flex", flexWrap: "wrap", gap: "5px" }}>
                    {station.docks.map((dock) => {
                    const hasBike = dock.bike !== null;
                    const isReserved = dock.bike?.status === "RESERVED"; // or BikeStatus.RESERVED if you have enum mapping

                    return (
                        <div
                        key={dock.dockId}

                        // Show bike ID on hover (with 'reserved' next to it if its reserved)
                        title={
                            hasBike
                            ? `Bike ID: ${dock.bike.bikeId}${isReserved ? " (Reserved)" : ""}`
                            : "Empty Dock"
                        }

                        // Style the boxes that represent the bikes
                        style={{
                            width: "22px",
                            height: "22px",
                            border: "1px solid #333",
                            backgroundColor: hasBike
                            ? isReserved
                                ? "#f44336" // red for reserved
                                : "#4caf50" // green for available bike
                            : "#fff", // empty dock (white)
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            fontSize: "10px",
                            color: "#000",
                            cursor: "pointer",
                        }}
                        >
                        {hasBike ? "B" : ""}
                        </div>
                    );
                    })}
                </div>
            </div>
        </Popup>
    </Marker>
  );
}

export default StationMarker;
