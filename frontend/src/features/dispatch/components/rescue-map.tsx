"use client";

import dynamic from "next/dynamic";
import type { RescueRequestSummary } from "@/features/request/types/request.types";
import type { Team, Warehouse } from "@/features/dispatch/types/dispatch.types";
import type { Vehicle } from "@/features/resource/services/resource.service";
import { useState } from "react";

// Dynamically import the map component to avoid SSR issues
const Map = dynamic(() => import("./map").then(mod => ({ default: mod.Map })), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 rounded-lg flex items-center justify-center">Đang tải bản đồ...</div>
});

const RequestMarker = dynamic(() => import("./request-marker").then(mod => ({ default: mod.RequestMarker })), { ssr: false });
const TeamMarker = dynamic(() => import("./team-marker").then(mod => ({ default: mod.TeamMarker })), { ssr: false });
const VehicleMarker = dynamic(() => import("./vehicle-marker").then(mod => ({ default: mod.VehicleMarker })), { ssr: false });
const WarehouseMarker = dynamic(() => import("./warehouse-marker").then(mod => ({ default: mod.WarehouseMarker })), { ssr: false });

interface RescueMapProps {
  requests: RescueRequestSummary[];
  teams: Team[];
  vehicles: Vehicle[];
  warehouses?: Warehouse[];
  onRequestClick?: (request: RescueRequestSummary) => void;
  onTeamClick?: (team: Team) => void;
  onVehicleClick?: (vehicle: Vehicle) => void;
  onWarehouseClick?: (warehouse: Warehouse) => void;
  className?: string;
  showToggles?: boolean;
}

export function RescueMap({
  requests,
  teams,
  vehicles,
  warehouses = [],
  onRequestClick,
  onTeamClick,
  onVehicleClick,
  onWarehouseClick,
  className,
  showToggles = true,
}: RescueMapProps) {
  const [visibleLayers, setVisibleLayers] = useState({
    requests: true,
    teams: true,
    vehicles: false,
    warehouses: true,
  });

  const toggleLayer = (layer: keyof typeof visibleLayers) => {
    setVisibleLayers(prev => ({ ...prev, [layer]: !prev[layer] }));
  };

  return (
    <div className={`relative ${className}`}>
      {showToggles && (
        <div className="absolute top-4 right-4 z-[1000] flex flex-col gap-2">
          <div className="bg-white/90 backdrop-blur-sm p-2 rounded-xl shadow-xl border border-slate-200 flex flex-col gap-1">
            <p className="text-[10px] font-bold text-slate-500 uppercase tracking-wider mb-1 px-1">Lớp bản đồ</p>
            <LayerButton 
              label="Yêu cầu" 
              active={visibleLayers.requests} 
              onClick={() => toggleLayer('requests')} 
              color="bg-red-500"
            />
            <LayerButton 
              label="Đội cứu hộ" 
              active={visibleLayers.teams} 
              onClick={() => toggleLayer('teams')} 
              color="bg-blue-600"
            />
            <LayerButton 
              label="Kho bãi" 
              active={visibleLayers.warehouses} 
              onClick={() => toggleLayer('warehouses')} 
              color="bg-teal-600"
            />
            <LayerButton 
              label="Phương tiện" 
              active={visibleLayers.vehicles} 
              onClick={() => toggleLayer('vehicles')} 
              color="bg-orange-500"
            />
          </div>
        </div>
      )}
      
      <Map className="h-full w-full rounded-2xl overflow-hidden shadow-inner border border-slate-200">
        {visibleLayers.requests && requests.map((request) => (
          <RequestMarker
            key={`request-${request.id}`}
            request={request}
            onClick={onRequestClick}
          />
        ))}
        {visibleLayers.teams && teams.map((team) => (
          <TeamMarker
            key={`team-${team.id}`}
            team={team}
            onClick={onTeamClick}
          />
        ))}
        {visibleLayers.vehicles && vehicles.map((vehicle) => (
          <VehicleMarker
            key={`vehicle-${vehicle.id}`}
            vehicle={vehicle}
            onClick={onVehicleClick}
          />
        ))}
        {visibleLayers.warehouses && warehouses.map((warehouse) => (
          <WarehouseMarker
            key={`warehouse-${warehouse.id}`}
            warehouse={warehouse}
            onClick={onWarehouseClick}
          />
        ))}
      </Map>
    </div>
  );
}

function LayerButton({ label, active, onClick, color }: { label: string; active: boolean; onClick: () => void; color: string }) {
  return (
    <button
      onClick={onClick}
      className={`flex items-center gap-2 px-2 py-1.5 rounded-lg text-xs font-semibold transition-all duration-200 ${
        active 
          ? 'bg-slate-100 text-slate-900 shadow-sm' 
          : 'bg-transparent text-slate-400 hover:bg-slate-50'
      }`}
    >
      <div className={`w-2 h-2 rounded-full ${active ? color : 'bg-slate-300'}`} />
      {label}
    </button>
  );
}