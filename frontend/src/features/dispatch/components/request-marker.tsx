"use client";

import { Marker, Popup } from "react-leaflet";
import { Icon } from "leaflet";
import "leaflet/dist/leaflet.css";
import type { RescueRequestSummary } from "@/features/request/types/request.types";

// Fix for default markers in react-leaflet
delete (Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

const urgencyColors = {
  CRITICAL: "#dc2626", // red-600
  HIGH: "#ea580c", // orange-600
  MEDIUM: "#ca8a04", // yellow-600
  LOW: "#16a34a", // green-600
} as const;

function createRequestIcon(urgency: keyof typeof urgencyColors) {
  return new Icon({
    iconUrl: `data:image/svg+xml;base64,${btoa(`
      <svg width="25" height="41" viewBox="0 0 25 41" xmlns="http://www.w3.org/2000/svg">
        <path d="M12.5 0C5.596 0 0 5.596 0 12.5c0 12.5 12.5 28.5 12.5 28.5s12.5-16 12.5-28.5C25 5.596 19.404 0 12.5 0z" fill="${urgencyColors[urgency]}"/>
        <circle cx="12.5" cy="12.5" r="5" fill="white"/>
      </svg>
    `)}`,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
  });
}

interface RequestMarkerProps {
  request: RescueRequestSummary;
  onClick?: (request: RescueRequestSummary) => void;
}

export function RequestMarker({ request, onClick }: RequestMarkerProps) {
  const icon = createRequestIcon(request.urgencyLevel);

  return (
    <Marker
      position={[request.lat, request.lng]}
      icon={icon}
      eventHandlers={{
        click: () => onClick?.(request),
      }}
    >
      <Popup>
        <div className="p-2">
          <h3 className="font-semibold text-sm">Yêu cầu cứu trợ #{request.id}</h3>
          <p className="text-xs text-gray-600 mt-1">{request.description}</p>
          <p className="text-xs mt-1">
            <span className="font-medium">Số người:</span> {request.numPeople}
          </p>
          <p className="text-xs">
            <span className="font-medium">Địa chỉ:</span> {request.addressText || "N/A"}
          </p>
          <p className="text-xs">
            <span className="font-medium">Trạng thái:</span> {request.status}
          </p>
          <p className="text-xs">
            <span className="font-medium">Mức độ:</span> {request.urgencyLevel}
          </p>
        </div>
      </Popup>
    </Marker>
  );
}