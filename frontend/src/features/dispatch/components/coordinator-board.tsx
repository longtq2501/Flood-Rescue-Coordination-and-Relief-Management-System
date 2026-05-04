"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { toast } from "sonner";
import Link from "next/link";
import { ClipboardList, Map as MapIcon, Search } from "lucide-react";

import {
  assignTeam,
  getTeams,
} from "@/features/dispatch/services/dispatch.service";
import {
  fetchCoordinatorRequests,
  verifyRequest,
} from "@/features/request/services/request.service";
import { getVehicles, getWarehouses } from "@/features/resource/services/resource.service";
import { RescueMap } from "./rescue-map";
import { RequestFilterBar } from "@/features/request/components/request-filter-bar";
import type { RescueRequestSummary, RequestStatus, UrgencyLevel, PageResult } from "@/features/request/types/request.types";
import type { Team, Warehouse } from "@/features/dispatch/types/dispatch.types";
import type { Vehicle } from "@/features/resource/types/resource.types";
import { motion } from "framer-motion";
import { cn } from "@/shared/utils/cn";
import { Button } from "@/components/ui/button";
import { MapPin, Clock, ShieldCheck, LayoutDashboard } from "lucide-react";

export function CoordinatorBoard() {
  const queryClient = useQueryClient();
  const searchParams = useSearchParams();
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null);
  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(null);

  // Extract filters from URL
  const filters = {
    status: (searchParams.get("status") as RequestStatus) || undefined,
    urgencyLevel: (searchParams.get("urgencyLevel") as UrgencyLevel) || undefined,
    search: searchParams.get("search") || undefined,
    fromDate: searchParams.get("fromDate") || undefined,
    toDate: searchParams.get("toDate") || undefined,
    page: Number(searchParams.get("page")) || 0,
  };

  const requestsQuery = useQuery({
    queryKey: ["coordinator-requests", filters],
    queryFn: () => fetchCoordinatorRequests(filters),
  });

  const teamsQuery = useQuery<Team[]>({
    queryKey: ["dispatch-teams"],
    queryFn: () => getTeams(),
  });

  const vehiclesQuery = useQuery<PageResult<Vehicle>>({
    queryKey: ["resource-vehicles"],
    queryFn: () => getVehicles(),
  });

  const warehousesQuery = useQuery<PageResult<Warehouse>>({
    queryKey: ["resource-warehouses"],
    queryFn: () => getWarehouses(),
  });

  const verifyMutation = useMutation({
    mutationFn: (id: number) => verifyRequest(String(id), "Verified from coordinator board"),
    onSuccess: () => {
      toast.success("Xác minh yêu cầu thành công");
      queryClient.invalidateQueries({ queryKey: ["coordinator-requests"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Xác minh yêu cầu thất bại");
    },
  });

  const assignMutation = useMutation({
    mutationFn: () => {
      if (!selectedRequestId || !selectedTeamId || !selectedVehicleId) {
        throw new Error("Cần chọn yêu cầu, đội cứu hộ và phương tiện");
      }
      return assignTeam({
        requestId: selectedRequestId,
        teamId: selectedTeamId,
        vehicleId: selectedVehicleId,
        note: "Demo assign",
      });
    },
    onSuccess: () => {
      toast.success("Phân công đội cứu hộ thành công");
      queryClient.invalidateQueries({ queryKey: ["coordinator-requests"] });
      setSelectedRequestId(null);
      setSelectedTeamId(null);
      setSelectedVehicleId(null);
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Phân công đội cứu hộ thất bại");
    },
  });

  const verifiedRequests = useMemo(
    () => requestsQuery.data?.content.filter((item) => item.status === "VERIFIED") ?? [],
    [requestsQuery.data],
  );

  return (
    <div className="space-y-4">
      <RequestFilterBar />

      <section className="rounded-3xl border border-slate-200 bg-white/50 backdrop-blur-sm p-6 soft-shadow">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <ClipboardList className="h-5 w-5 text-teal-600" />
            Yêu cầu cứu trợ mới
          </h2>
          <span className="text-xs font-medium text-slate-400">
            Tổng số: {requestsQuery.data?.totalElements || 0}
          </span>
        </div>

        <div className="space-y-4">
          {requestsQuery.isLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-3">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-teal-600 border-t-transparent" />
              <p className="text-sm font-medium text-slate-500">Đang tải danh sách...</p>
            </div>
          ) : requestsQuery.data?.content.length === 0 ? (
            <div className="py-12 text-center">
              <div className="inline-flex h-16 w-16 items-center justify-center rounded-full bg-slate-50 mb-4 text-slate-300">
                <Search className="h-8 w-8" />
              </div>
              <p className="text-slate-500 font-medium">Không tìm thấy yêu cầu nào</p>
            </div>
          ) : (
            requestsQuery.data?.content.map((item) => (
              <motion.article
                layout
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                key={item.id}
                className="group relative rounded-2xl border border-slate-100 bg-white p-4 transition-all hover:border-teal-200 hover:shadow-md"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-bold text-teal-600 uppercase tracking-wider">
                        #{item.id}
                      </span>
                      <span className={cn(
                        "rounded-full px-2 py-0.5 text-[10px] font-bold uppercase",
                        item.urgencyLevel === "CRITICAL" ? "bg-red-100 text-red-700" :
                          item.urgencyLevel === "HIGH" ? "bg-orange-100 text-orange-700" :
                            "bg-blue-100 text-blue-700"
                      )}>
                        {item.urgencyLevel}
                      </span>
                    </div>
                    <h3 className="font-bold text-slate-900 group-hover:text-teal-700 transition-colors">
                      {item.citizenName || 'Người dân ẩn danh'}
                    </h3>
                    <p className="mt-1 text-sm text-slate-600 line-clamp-2 leading-relaxed">
                      {item.description}
                    </p>
                    <div className="mt-3 flex items-center gap-4 text-[11px] text-slate-400">
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3" />
                        <span>{item.addressText || 'Không có địa chỉ'}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        <span>{new Date(item.createdAt || '').toLocaleString('vi-VN')}</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-col items-end gap-2">
                    <span className={cn(
                      "rounded-lg px-2.5 py-1 text-xs font-bold shadow-sm",
                      item.status === "PENDING" ? "bg-amber-50 text-amber-700 border border-amber-100" :
                        item.status === "VERIFIED" ? "bg-teal-50 text-teal-700 border border-teal-100" :
                          "bg-slate-50 text-slate-600"
                    )}>
                      {item.status}
                    </span>
                  </div>
                </div>

                <div className="mt-4 flex items-center justify-end gap-3 pt-3 border-t border-slate-50">
                  <Button
                    variant="ghost"
                    disabled={item.status !== "PENDING" || verifyMutation.isPending}
                    onClick={() => verifyMutation.mutate(item.id)}
                    className="h-8 text-xs font-bold text-teal-700 hover:bg-teal-50 rounded-lg px-3"
                  >
                    <ShieldCheck className="mr-1.5 h-3.5 w-3.5" />
                    Xác minh
                  </Button>
                  <Button
                    onClick={() => setSelectedRequestId(item.id)}
                    className={cn(
                      "h-8 text-xs font-bold rounded-lg shadow-sm transition-all px-3",
                      selectedRequestId === item.id
                        ? "bg-teal-600 text-white"
                        : "bg-slate-100 text-slate-700 hover:bg-slate-200"
                    )}
                  >
                    <LayoutDashboard className="mr-1.5 h-3.5 w-3.5" />
                    {selectedRequestId === item.id ? "Đã chọn" : "Phân công"}
                  </Button>
                </div>
              </motion.article>
            ))
          )}
        </div>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-slate-900">Bảng phân công</h2>
        <p className="mt-1 text-sm text-slate-600">Chỉ phân công các yêu cầu đã XÁC MINH.</p>
        <div className="mt-3 grid gap-3 md:grid-cols-3">
          <select
            className="rounded-xl border border-slate-300 px-3 py-2"
            value={selectedRequestId ?? ""}
            onChange={(event) => setSelectedRequestId(Number(event.target.value) || null)}
          >
            <option value="">Chọn yêu cầu</option>
            {verifiedRequests.map((item) => (
              <option key={item.id} value={item.id}>
                #{item.id} - {item.urgencyLevel}
              </option>
            ))}
          </select>

          <select
            className="rounded-xl border border-slate-300 px-3 py-2"
            value={selectedTeamId ?? ""}
            onChange={(event) => setSelectedTeamId(Number(event.target.value) || null)}
          >
            <option value="">Chọn đội cứu hộ</option>
            {teamsQuery.data?.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({item.status})
              </option>
            ))}
          </select>

          <select
            className="rounded-xl border border-slate-300 px-3 py-2"
            value={selectedVehicleId ?? ""}
            onChange={(event) => setSelectedVehicleId(Number(event.target.value) || null)}
          >
            <option value="">Chọn phương tiện</option>
            {vehiclesQuery.data?.content.map((item) => (
              <option key={item.id} value={item.id}>
                {item.plateNumber} ({item.type})
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={() => assignMutation.mutate()}
          disabled={assignMutation.isPending}
          className="mt-3 rounded-xl bg-teal-700 px-4 py-2 text-sm font-semibold text-white disabled:opacity-60"
        >
          {assignMutation.isPending ? "Đang phân công..." : "Phân công ngay"}
        </button>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-slate-900">Bản đồ cứu trợ</h2>
        <p className="mt-1 text-sm text-slate-600">
          Theo dõi vị trí yêu cầu cứu trợ, đội cứu hộ và phương tiện.
        </p>
        <div className="mt-3 h-96 w-full rounded-2xl overflow-hidden border border-slate-200 shadow-inner">
          <RescueMap
            requests={requestsQuery.data?.content ?? []}
            teams={teamsQuery.data ?? []}
            vehicles={vehiclesQuery.data?.content ?? []}
            warehouses={warehousesQuery.data?.content ?? []}
            onRequestClick={(request: RescueRequestSummary) => {
              toast.info(`Yêu cầu #${request.id}: ${request.description}`);
            }}
            onTeamClick={(team: Team) => {
              toast.info(`Đội ${team.name}: ${team.status}`);
            }}
            onVehicleClick={(vehicle: Vehicle) => {
              toast.info(`Phương tiện ${vehicle.plateNumber}: ${vehicle.status}`);
            }}
            onWarehouseClick={(warehouse: Warehouse) => {
              toast.info(`Kho ${warehouse.name}: ${warehouse.location}`);
            }}
          />
        </div>
      </section>
    </div>
  );
}
