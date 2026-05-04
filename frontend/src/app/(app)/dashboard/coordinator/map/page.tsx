"use client";

import { useQuery } from "@tanstack/react-query";
import { toast } from "sonner";
import { useRouter } from "next/navigation";
import { ArrowLeft, RefreshCw } from "lucide-react";

import { getMapData } from "@/features/dispatch/services/dispatch.service";
import { RescueMap } from "@/features/dispatch/components/rescue-map";
import { getVehicles } from "@/features/resource/services/resource.service";

export default function FullMapPage() {
  const router = useRouter();

  const mapDataQuery = useQuery({
    queryKey: ["map-data-full"],
    queryFn: getMapData,
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  const vehiclesQuery = useQuery({
    queryKey: ["resource-vehicles-full"],
    queryFn: getVehicles,
  });

  const isLoading = mapDataQuery.isLoading || vehiclesQuery.isLoading;

  return (
    <div className="flex flex-col h-[calc(100vh-64px)] -m-4 md:-m-8">
      {/* Header Bar */}
      <div className="bg-white border-b border-slate-200 px-4 py-3 flex items-center justify-between shadow-sm z-10">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => router.back()}
            className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-600"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">Bản đồ Điều phối Toàn diện</h1>
            <p className="text-xs text-slate-500 font-medium">Cập nhật thời gian thực các nguồn lực cứu trợ</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden md:flex items-center gap-4 px-4 py-1.5 bg-slate-50 rounded-full border border-slate-200 mr-2">
            <StatItem label="Yêu cầu" value={mapDataQuery.data?.requests.length ?? 0} color="text-red-600" />
            <StatItem label="Đội" value={mapDataQuery.data?.teams.length ?? 0} color="text-blue-600" />
            <StatItem label="Kho" value={mapDataQuery.data?.warehouses.length ?? 0} color="text-teal-600" />
          </div>

          <button 
            onClick={() => mapDataQuery.refetch()}
            disabled={mapDataQuery.isFetching}
            className="flex items-center gap-2 bg-white border border-slate-300 px-3 py-2 rounded-xl text-sm font-semibold text-slate-700 hover:bg-slate-50 transition-colors shadow-sm disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${mapDataQuery.isFetching ? 'animate-spin' : ''}`} />
            Làm mới
          </button>
        </div>
      </div>

      {/* Main Map Area */}
      <div className="flex-1 relative bg-slate-100 overflow-hidden">
        {isLoading ? (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-4 z-20 bg-slate-50/80 backdrop-blur-sm">
            <div className="w-12 h-12 border-4 border-teal-600 border-t-transparent rounded-full animate-spin" />
            <p className="text-slate-600 font-bold animate-pulse">Đang tải dữ liệu bản đồ...</p>
          </div>
        ) : (
          <RescueMap
            className="h-full w-full"
            requests={mapDataQuery.data?.requests ?? []}
            teams={mapDataQuery.data?.teams ?? []}
            vehicles={vehiclesQuery.data?.content ?? []}
            warehouses={mapDataQuery.data?.warehouses ?? []}
            onRequestClick={(req) => toast.info(`Yêu cầu #${req.id}: ${req.urgencyLevel}`)}
            onTeamClick={(team) => toast.info(`Đội ${team.name}: ${team.status}`)}
            onWarehouseClick={(w) => toast.info(`Kho ${w.name}`)}
            showToggles={true}
          />
        )}
      </div>
    </div>
  );
}

function StatItem({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{label}:</span>
      <span className={`text-sm font-bold ${color}`}>{value}</span>
    </div>
  );
}
