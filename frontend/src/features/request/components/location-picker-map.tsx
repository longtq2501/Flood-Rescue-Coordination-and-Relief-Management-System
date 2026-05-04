"use client";

import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { useEffect, useState } from "react";

// Fix Leaflet default icon issue in Next.js
const DefaultIcon = L.icon({
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

L.Marker.prototype.options.icon = DefaultIcon;

interface LocationPickerMapProps {
  lat?: number;
  lng?: number;
  onChange: (lat: number, lng: number) => void;
}

function MapEvents({ onChange }: { onChange: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onChange(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

// Helper component to recenter map when lat/lng change externally
function RecenterMap({ lat, lng }: { lat?: number; lng?: number }) {
  const map = useMap();
  useEffect(() => {
    if (lat && lng) {
      map.setView([lat, lng], map.getZoom());
    }
  }, [lat, lng, map]);
  return null;
}

export default function LocationPickerMap({ lat, lng, onChange }: LocationPickerMapProps) {
  const defaultCenter: [number, number] = [10.762622, 106.660172]; // Default to HCMC

  return (
    <div className="h-64 w-full overflow-hidden rounded-xl border border-slate-300 shadow-inner">
      <MapContainer
        center={lat && lng ? [lat, lng] : defaultCenter}
        zoom={13}
        className="h-full w-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <MapEvents onChange={onChange} />
        <RecenterMap lat={lat} lng={lng} />
        {lat && lng && (
          <Marker position={[lat, lng]} />
        )}
      </MapContainer>
    </div>
  );
}
