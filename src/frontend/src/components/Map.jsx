import { MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import StationMarker from "./stationMarker/StationMarker";
import { useEffect, useState } from "react";

// This ensures that default Leaflet markers render correctly (sometimes React breaks the implicit defaults)
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require("leaflet/dist/images/marker-icon-2x.png"), // For high-Dpi screens (Rich ppl with fancy screens)
  iconUrl: require("leaflet/dist/images/marker-icon.png"), // Normal icon
  shadowUrl: require("leaflet/dist/images/marker-shadow.png"), // Shadow under the marker
});

const Map = ({
  onClickShowConfirmRental,
  activeBikeRental,
  onClickShowConfirmReturn,
  stations: initialStations,
}) => {
  // Center of the map, where the map will render first essentially
  const center = [45.552648, -73.681342]; // These are the coords of Montreal, kinda (found online)
  const [stations, setStations] = useState(initialStations || []);
  // Map size, Leaflet needs a fixed height (forced) or it wont appear
  const size = {
    height: "500px",
    width: "50%",
  };

  useEffect(() => {
    const eventSource = new EventSource(
      "http://localhost:8080/api/stations/stream"
    );

    eventSource.addEventListener("station-update", (event) => {
      const updatedStation = JSON.parse(event.data);

      // Update the stations state with the new station data
      setStations((prevStations) =>
        prevStations.map((station) =>
          station.stationId === updatedStation.stationId
            ? updatedStation
            : station
        )
      );
    });
    eventSource.onerror = (error) => {
      console.error("SSE error:", error);
    };

    // Cleanup function - runs when component unmounts
    return () => {
      eventSource.close();
    };
  }, []);

  useEffect(() => {
    setStations(initialStations || []);
  }, [initialStations]);

  return (
    <MapContainer center={center} zoom={11} style={size}>
      {/* Maps generally use tiles so they dont have to render the whole world and only what fits in the map display, hence this import by Leaflet for rendering*/}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Dynamically display all markers for stations on map */}
      {stations &&
        stations.map((station) => (
          <StationMarker
            key={`${station.stationId}-${activeBikeRental.bikeId || "none"}`}
            station={station}
            onClickShowConfirmRental={onClickShowConfirmRental}
            activeBikeRental={activeBikeRental}
            onClickShowConfirmReturn={onClickShowConfirmReturn}
          />
        ))}
    </MapContainer>
  );
};

export default Map;
