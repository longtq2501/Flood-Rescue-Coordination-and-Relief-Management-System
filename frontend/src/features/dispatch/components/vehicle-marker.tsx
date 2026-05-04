"use client";

import { Marker, Popup } from "react-leaflet";
import { Icon } from "leaflet";
import type { Vehicle } from "@/features/resource/services/resource.service";

// Fix for default markers in react-leaflet
delete (Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

const vehicleIcons = {
  BOAT: "🚤",
  TRUCK: "🚛",
  HELICOPTER: "🚁",
  AMBULANCE: "🚑",
  OTHER: "🚗",
} as const;

const statusColors = {
  AVAILABLE: "#16a34a", // green-600
  IN_USE: "#dc2626", // red-600
  MAINTENANCE: "#ea580c", // orange-600
  OFFLINE: "#6b7280", // gray-500
} as const;

function createVehicleIcon(type: keyof typeof vehicleIcons, status: keyof typeof statusColors) {
  const emoji = vehicleIcons[type];
  return new Icon({
    iconUrl: `data:image/svg+xml;base64,${btoa(`
      <svg width="35" height="35" viewBox="0 0 35 35" xmlns="http://www.w3.org/2000/svg">
        <rect x="2" y="2" width="31" height="31" rx="4" fill="${statusColors[status]}" stroke="white" stroke-width="2"/>
        <text x="17.5" y="22" font-family="Arial, sans-serif" font-size="16" fill="white" text-anchor="middle">${emoji}</text>
      </svg>
    `)}`,
    iconSize: [35, 35],
    iconAnchor: [17, 17],
    popupAnchor: [0, -17],
  });
}

interface VehicleMarkerProps {
  vehicle: Vehicle;
  onClick?: (vehicle: Vehicle) => void;
}

export function VehicleMarker({ vehicle, onClick }: VehicleMarkerProps) {
  // Skip rendering if no location data
  if (!vehicle.lat || !vehicle.lng) return null;

  const icon = createVehicleIcon(vehicle.type, vehicle.status);

  return (
    <Marker
      position={[vehicle.lat, vehicle.lng]}
      icon={icon}
      eventHandlers={{
        click: () => onClick?.(vehicle),
      }}
    >
      <Popup>
        <div className="p-2">
          <h3 className="font-semibold text-sm">Phương tiện: {vehicle.plateNumber}</h3>
          <p className="text-xs mt-1">
            <span className="font-medium">Loại:</span> {vehicle.type}
          </p>
          <p className="text-xs">
            <span className="font-medium">Trạng thái:</span> {vehicle.status}
          </p>
          <p className="text-xs">
            <span className="font-medium">Sức chứa:</span> {vehicle.capacity}
          </p>
        </div>
      </Popup>
    </Marker>
  );
}