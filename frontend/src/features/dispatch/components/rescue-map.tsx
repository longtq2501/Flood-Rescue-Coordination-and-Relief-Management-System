"use client";

import dynamic from "next/dynamic";
import type { RescueRequestSummary } from "@/features/request/types/request.types";
import type { Team } from "@/features/dispatch/types/dispatch.types";
import type { Vehicle } from "@/features/resource/services/resource.service";

// Dynamically import the map component to avoid SSR issues
const Map = dynamic(() => import("./map").then(mod => ({ default: mod.Map })), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 rounded-lg flex items-center justify-center">Đang tải bản đồ...</div>
});

const RequestMarker = dynamic(() => import("./request-marker").then(mod => ({ default: mod.RequestMarker })), { ssr: false });
const TeamMarker = dynamic(() => import("./team-marker").then(mod => ({ default: mod.TeamMarker })), { ssr: false });
const VehicleMarker = dynamic(() => import("./vehicle-marker").then(mod => ({ default: mod.VehicleMarker })), { ssr: false });

interface RescueMapProps {
  requests: RescueRequestSummary[];
  teams: Team[];
  vehicles: Vehicle[];
  onRequestClick?: (request: RescueRequestSummary) => void;
  onTeamClick?: (team: Team) => void;
  onVehicleClick?: (vehicle: Vehicle) => void;
  className?: string;
}

export function RescueMap({
  requests,
  teams,
  vehicles,
  onRequestClick,
  onTeamClick,
  onVehicleClick,
  className,
}: RescueMapProps) {
  return (
    <Map className={className}>
      {requests.map((request) => (
        <RequestMarker
          key={`request-${request.id}`}
          request={request}
          onClick={onRequestClick}
        />
      ))}
      {teams.map((team) => (
        <TeamMarker
          key={`team-${team.id}`}
          team={team}
          onClick={onTeamClick}
        />
      ))}
      {vehicles.map((vehicle) => (
        <VehicleMarker
          key={`vehicle-${vehicle.id}`}
          vehicle={vehicle}
          onClick={onVehicleClick}
        />
      ))}
    </Map>
  );
}