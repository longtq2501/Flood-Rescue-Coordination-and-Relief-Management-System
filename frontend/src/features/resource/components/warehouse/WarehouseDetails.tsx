"use client";

import { useQuery } from "@tanstack/react-query";
import { getWarehouseDetails } from "@/features/resource/services/resource.service";
import { Button } from "@/components/ui/button";
import { MapPin, Box, ArrowLeft } from "lucide-react";
import Link from "next/link";

function formatNumber(n: number) {
  try {
    return new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 }).format(n);
  } catch (e) {
    return String(n);
  }
}

interface WarehouseDetailsProps {
  id: number;
}

export function WarehouseDetails({ id }: WarehouseDetailsProps) {
  const { data: warehouse, isLoading, isError, error } = useQuery({
    queryKey: ["warehouse", id],
    queryFn: () => getWarehouseDetails(id),
  });

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500">Loading warehouse details...</div>;
  }

  if (isError || !warehouse) {
    return (
      <div className="p-8 text-center text-red-500">
        Error: {error instanceof Error ? error.message : "Warehouse not found"}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link href="/dashboard/manager/warehouses">
          <Button variant="ghost" icon={ArrowLeft}>Back</Button>
        </Link>
        <h1 className="text-2xl font-bold text-slate-900">{warehouse.name}</h1>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-slate-900">General Information</h2>
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <div className="mt-1 flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500">
                <MapPin className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-slate-500">Location</p>
                <p className="text-slate-900">{warehouse.location}</p>
                {warehouse.lat && warehouse.lng && (
                  <p className="text-xs text-slate-400 mt-1">
                    Coordinates: {warehouse.lat.toFixed(4)}, {warehouse.lng.toFixed(4)}
                  </p>
                )}
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="mt-1 flex h-8 w-8 items-center justify-center rounded-lg bg-slate-100 text-slate-500">
                <Box className="h-5 w-5" />
              </div>
              <div>
                <p className="text-sm font-medium text-slate-500">Total Capacity</p>
                <p className="text-slate-900">{formatNumber(warehouse.capacity)} units</p>
              </div>
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-slate-900">Status & Analytics</h2>
          <div className="flex h-32 items-center justify-center rounded-lg bg-slate-50 border border-dashed border-slate-300 text-center px-4">
            <p className="text-sm text-slate-500">Inventory visualization and supply tracking coming soon in the next update.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
