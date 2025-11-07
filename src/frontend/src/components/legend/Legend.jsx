import * as React from "react";
import "./Legend.css";
import stationAvail from "../../assets/StationMarkerAvailable.png";
import freeBike from "../../assets/FreeBike.png";
import reservedBike from "../../assets/ReservedBike.png";

export default function Legend() {
  const legendItems = [
    { icon: stationAvail, label: "Station is functional" },
    { icon: freeBike, label: "Bike available for renting" },
    { icon: reservedBike, label: "Bike is reserved" },
  ];

  return (
    <div className="legend-container">
      <h3 className="legend-title">Map icons legend</h3>
      <div className="legend-items">
        {legendItems.map((item, index) => (
          <div key={index} className="legend-item">
            <img src={item.icon} alt={item.label} className="legend-icon" />
            <span className="legend-label">{item.label}</span>
          </div>
        ))}
        <span className="legend-label">Bike Type: E_BIKE and STANDARD have different pricing, please look out</span>
      </div>
    </div>
  );
}
