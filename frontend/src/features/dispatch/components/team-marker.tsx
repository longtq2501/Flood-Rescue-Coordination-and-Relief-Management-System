"use client";

import { Marker, Popup } from "react-leaflet";
import { Icon } from "leaflet";
import type { Team } from "@/features/dispatch/types/dispatch.types";

// Fix for default markers in react-leaflet
delete (Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png",
  iconUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png",
  shadowUrl: "https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png",
});

const statusColors = {
  AVAILABLE: "#16a34a", // green-600
  BUSY: "#dc2626", // red-600
  RETURNING: "#ea580c", // orange-600
  OFFLINE: "#6b7280", // gray-500
} as const;

function createTeamIcon(status: keyof typeof statusColors) {
  return new Icon({
    iconUrl: `data:image/svg+xml;base64,${btoa(`
      <svg width="30" height="30" viewBox="0 0 30 30" xmlns="http://www.w3.org/2000/svg">
        <circle cx="15" cy="15" r="12" fill="${statusColors[status]}" stroke="white" stroke-width="2"/>
        <path d="M8 12h14v6H8z" fill="white"/>
        <circle cx="12" cy="15" r="1.5" fill="${statusColors[status]}"/>
        <circle cx="18" cy="15" r="1.5" fill="${statusColors[status]}"/>
      </svg>
    `)}`,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
    popupAnchor: [0, -15],
  });
}

interface TeamMarkerProps {
  team: Team;
  onClick?: (team: Team) => void;
}

export function TeamMarker({ team, onClick }: TeamMarkerProps) {
  // Skip rendering if no location data
  if (!team.lat || !team.lng) return null;

  const icon = createTeamIcon(team.status);

  return (
    <Marker
      position={[team.lat, team.lng]}
      icon={icon}
      eventHandlers={{
        click: () => onClick?.(team),
      }}
    >
      <Popup>
        <div className="p-2">
          <h3 className="font-semibold text-sm">Đội cứu hộ: {team.name}</h3>
          <p className="text-xs mt-1">
            <span className="font-medium">Trạng thái:</span> {team.status}
          </p>
          <p className="text-xs">
            <span className="font-medium">Sức chứa:</span> {team.capacity}
          </p>
          <p className="text-xs">
            <span className="font-medium">Số thành viên:</span> {team.memberCount}
          </p>
        </div>
      </Popup>
    </Marker>
  );
}