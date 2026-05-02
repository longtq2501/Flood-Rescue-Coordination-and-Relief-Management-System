"use client";

import { MapContainer, TileLayer } from "react-leaflet";
import MarkerClusterGroup from "react-leaflet-markercluster";
import "leaflet/dist/leaflet.css";
import "react-leaflet-markercluster/styles";

interface MapProps {
  className?: string;
  children?: React.ReactNode;
  enableClustering?: boolean;
}

export function Map({ className, children, enableClustering = true }: MapProps) {
  return (
    <MapContainer
      center={[10.8231, 106.6297]} // Default to Ho Chi Minh City
      zoom={10}
      className={className}
      style={{ height: "100%", width: "100%" }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      {enableClustering ? (
        <MarkerClusterGroup>
          {children}
        </MarkerClusterGroup>
      ) : (
        children
      )}
    </MapContainer>
  );
}