import { MapContainer, TileLayer } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import StationMarker from "./stationMarker/StationMarker";
import { useEffect, useState } from "react";

// Custom markers, for dynamic coloring
const redDivIcon = L.divIcon({
  html: '<div style="background-color: red; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>',
  className: '', // prevents Leaflet's default icon styling
  iconSize: [20, 20]
});
const yellowDivIcon = L.divIcon({
  html: '<div style="background-color: yellow; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>',
  className: '', // prevents Leaflet's default icon styling
  iconSize: [20, 20]
});
const greenDivIcon = L.divIcon({
  html: '<div style="background-color: green; width: 20px; height: 20px; border-radius: 50%; border: 2px solid white;"></div>',
  className: '', // prevents Leaflet's default icon styling
  iconSize: [20, 20]
});

const Map = ({
  onClickShowConfirmRental,
  activeBikeRental,
  onClickShowConfirmReturn, 
  onClickShowConfirmReservation,
  onClickShowCancelReservation,
  activeReservation,
  stations: initialStations,
  toggleStationStatus, 
  userRole, 
  rebalanceBike,
  handleBikeMaintain,
  bikesUnderMaintenance,
  setActiveBikeMaintenanceRemoval,
  activeBikeMaintenanceRemoval,
  handleRemoveFromMaintenance
}) => {
  // Center of the map, where the map will render first essentially
  const center = [45.552648, -73.681342]; // These are the coords of Montreal, kinda (found online)
  const [stations, setStations] = useState(initialStations || []);
  // Map size and other styles, Leaflet needs a fixed height (forced) or it wont appear
  const style = {
    height: "500px",
    zIndex: 0
  };

  // bike source for rebalancing, done across whole map so that it works across stations
  // not sending calls to database in case operation interrupted
  const [rebalanceSource, setRebalanceSource] = useState({
      bikeId: null,
      sourceDockId: null,
      sourceStationId: null
  });

  useEffect(() => {
    setStations(initialStations || []);
  }, [initialStations]);

  // setting bike source for rebalancing
  const handleRebalanceSource = (bike, dock, stationId) => {
      setRebalanceSource({
          bikeId: bike.bikeId,
          sourceDockId: dock.dockId,
          sourceStationId: stationId
      });
  };

  // rebalance to a target dock, full dto used
  const handleRebalanceTarget = async (targetDock, targetStationId) => {
      if (!rebalanceSource.bikeId) return;
      
      const entireRebalance = {
          bikeId: rebalanceSource.bikeId,
          sourceDockId: rebalanceSource.sourceDockId,
          sourceStationId: rebalanceSource.sourceStationId,
          targetDockId: targetDock.dockId,
          targetStationId: targetStationId
      };
      
      try {
          await rebalanceBike(entireRebalance);
          setRebalanceSource({ bikeId: null, sourceDockId: null, sourceStationId: null });
      } catch (error) {
          console.error("Failed to rebalance bike:", error);
      }
  };

  return (
    <MapContainer center={center} zoom={11} style={style}>
      {/* Maps generally use tiles so they dont have to render the whole world and only what fits in the map display, hence this import by Leaflet for rendering*/}
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Dynamically display all markers for stations on map */}
      {stations &&
        stations.map((station) => {
          let icon;
          const ratio = Number(station.numberOfBikesDocked) / Number(station.capacity);

          if (station.numberOfBikesDocked === station.capacity || station.numberOfBikesDocked === 0) {
            icon = redDivIcon;
          } else if (ratio > 0.85 || ratio < 0.25) {
            icon = yellowDivIcon;
          } else {
            icon = greenDivIcon;
          }

          // console.log(`DEBUG: COLOR OF MARKER FOR STATION ${station.stationId}: ${ratio}`)
          return (
            <StationMarker
              key={`${station.stationId}-${activeBikeRental.bikeId || "none"}`}
              station={station}
              icon={icon}
              onClickShowConfirmRental={onClickShowConfirmRental}
              activeBikeRental={activeBikeRental}
              onClickShowConfirmReturn={onClickShowConfirmReturn}
              toggleStationStatus={toggleStationStatus}
              userRole={userRole}
              rebalanceSource={rebalanceSource}
              handleRebalanceSource={handleRebalanceSource}
              handleRebalanceTarget={handleRebalanceTarget}
              onClickShowConfirmReservation={onClickShowConfirmReservation}
              onClickShowCancelReservation={onClickShowCancelReservation}
              activeReservation={activeReservation}
              handleBikeMaintain={handleBikeMaintain}
              bikesUnderMaintenance={bikesUnderMaintenance}
              setActiveBikeMaintenanceRemoval={setActiveBikeMaintenanceRemoval}
              activeBikeMaintenanceRemoval={activeBikeMaintenanceRemoval}
              handleRemoveFromMaintenance={handleRemoveFromMaintenance}
            />
          );
        })};
    </MapContainer>
  );
};

export default Map;