"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import type { RescueRequestSummary } from "@/features/request/types/request.types";
import type { Team, Warehouse } from "@/features/dispatch/types/dispatch.types";
import type { Vehicle } from "@/features/resource/types/resource.types";
import { Layers, MapPin, Shield, Truck, Warehouse as WarehouseIcon } from "lucide-react";
import { cn } from "@/shared/utils/cn";

// Dynamically import the map component to avoid SSR issues
const Map = dynamic(() => import("./map").then(mod => ({ default: mod.Map })), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-slate-50 rounded-2xl flex items-center justify-center border-2 border-dashed border-slate-200">
    <div className="flex flex-col items-center gap-3">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-teal-600 border-t-transparent" />
      <p className="text-sm font-medium text-slate-500">Đang chuẩn bị bản đồ...</p>
    </div>
  </div>
});

const RequestMarker = dynamic(() => import("./request-marker").then(mod => ({ default: mod.RequestMarker })), { ssr: false });
const TeamMarker = dynamic(() => import("./team-marker").then(mod => ({ default: mod.TeamMarker })), { ssr: false });
const VehicleMarker = dynamic(() => import("./vehicle-marker").then(mod => ({ default: mod.VehicleMarker })), { ssr: false });
const WarehouseMarker = dynamic(() => import("./warehouse-marker").then(mod => ({ default: mod.WarehouseMarker })), { ssr: false });

interface RescueMapProps {
  requests: RescueRequestSummary[];
  teams: Team[];
  vehicles: Vehicle[];
  warehouses: Warehouse[];
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
  warehouses,
  onRequestClick,
  onTeamClick,
  onVehicleClick,
  onWarehouseClick,
  className,
  showToggles = false,
}: RescueMapProps) {
  const [layers, setLayers] = useState({
    requests: true,
    teams: true,
    vehicles: true,
    warehouses: true,
  });

  return (
    <div className={cn("relative h-full w-full", className)}>
      <Map className="h-full w-full">
        {layers.requests && requests.map((request) => (
          <RequestMarker
            key={`request-${request.id}`}
            request={request}
            onClick={onRequestClick}
          />
        ))}
        {layers.teams && teams.map((team) => (
          <TeamMarker
            key={`team-${team.id}`}
            team={team}
            onClick={onTeamClick}
          />
        ))}
        {layers.vehicles && vehicles.map((vehicle) => (
          <VehicleMarker
            key={`vehicle-${vehicle.id}`}
            vehicle={vehicle}
            onClick={onVehicleClick}
          />
        ))}
        {layers.warehouses && warehouses.map((warehouse) => (
          <WarehouseMarker
            key={`warehouse-${warehouse.id}`}
            warehouse={warehouse}
            onClick={onWarehouseClick}
          />
        ))}
      </Map>

      {showToggles && (
        <div className="absolute top-4 right-4 z-[1000] flex flex-col gap-2">
          <div className="bg-white/90 backdrop-blur-md p-3 rounded-2xl shadow-xl border border-white/20">
            <div className="flex items-center gap-2 mb-3 px-1">
              <Layers className="w-4 h-4 text-slate-500" />
              <span className="text-xs font-bold text-slate-700 uppercase tracking-wider">Lớp bản đồ</span>
            </div>
            
            <div className="flex flex-col gap-1">
              <LayerToggle 
                label="Yêu cầu" 
                active={layers.requests} 
                icon={MapPin} 
                color="text-red-500"
                onClick={() => setLayers(l => ({ ...l, requests: !l.requests }))} 
              />
              <LayerToggle 
                label="Đội cứu hộ" 
                active={layers.teams} 
                icon={Shield} 
                color="text-blue-500"
                onClick={() => setLayers(l => ({ ...l, teams: !l.teams }))} 
              />
              <LayerToggle 
                label="Phương tiện" 
                active={layers.vehicles} 
                icon={Truck} 
                color="text-amber-500"
                onClick={() => setLayers(l => ({ ...l, vehicles: !l.vehicles }))} 
              />
              <LayerToggle 
                label="Kho hàng" 
                active={layers.warehouses} 
                icon={WarehouseIcon} 
                color="text-teal-500"
                onClick={() => setLayers(l => ({ ...l, warehouses: !l.warehouses }))} 
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function LayerToggle({ label, active, icon: Icon, color, onClick }: { 
  label: string; 
  active: boolean; 
  icon: any; 
  color: string;
  onClick: () => void 
}) {
  return (
    <button 
      onClick={onClick}
      className={cn(
        "flex items-center gap-3 px-3 py-2 rounded-xl text-sm font-medium transition-all w-full",
        active ? "bg-slate-100 text-slate-900 shadow-sm" : "text-slate-400 hover:bg-slate-50"
      )}
    >
      <Icon className={cn("w-4 h-4", active ? color : "text-slate-300")} />
      <span>{label}</span>
      <div className={cn(
        "ml-auto w-2 h-2 rounded-full",
        active ? "bg-green-500" : "bg-slate-200"
      )} />
    </button>
  );
}
