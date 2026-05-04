"use client";

import { Marker, Popup } from "react-leaflet";
import L from "leaflet";
import type { Warehouse } from "@/features/dispatch/types/dispatch.types";

const warehouseIcon = L.divIcon({
  className: "custom-div-icon",
  html: `<div class="flex items-center justify-center w-8 h-8 bg-teal-600 rounded-lg shadow-lg border-2 border-white">
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
  </div>`,
  iconSize: [32, 32],
  iconAnchor: [16, 16],
});

interface WarehouseMarkerProps {
  warehouse: Warehouse;
  onClick?: (warehouse: Warehouse) => void;
}

export function WarehouseMarker({ warehouse, onClick }: WarehouseMarkerProps) {
  if (!warehouse.lat || !warehouse.lng) return null;

  return (
    <Marker
      position={[warehouse.lat, warehouse.lng]}
      icon={warehouseIcon}
      eventHandlers={{
        click: () => onClick?.(warehouse),
      }}
    >
      <Popup className="rounded-xl">
        <div className="p-1">
          <h3 className="font-bold text-slate-900">{warehouse.name}</h3>
          <p className="text-xs text-slate-600 mt-1">{warehouse.address}</p>
          <div className="mt-2 pt-2 border-t border-slate-100 flex justify-between items-center">
            <span className="text-[10px] font-semibold uppercase tracking-wider text-teal-600 bg-teal-50 px-1.5 py-0.5 rounded">
              Warehouse
            </span>
            <a 
              href={`/dashboard/manager/warehouses/${warehouse.id}`}
              className="text-xs font-semibold text-cyan-700 hover:underline"
            >
              Chi tiết →
            </a>
          </div>
        </div>
      </Popup>
    </Marker>
  );
}
