 "use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "next/navigation";
import { toast } from "sonner";
import Link from "next/link";
import { Map as MapIcon } from "lucide-react";

import {
  assignTeam,
  getTeams,
} from "@/features/dispatch/services/dispatch.service";
import {
  fetchCoordinatorRequests,
  verifyRequest,
} from "@/features/request/services/request.service";
import { getVehicles } from "@/features/resource/services/resource.service";
import { RescueMap } from "./rescue-map";
import { RequestFilterBar } from "@/features/request/components/request-filter-bar";
import type { RescueRequestSummary, RequestStatus, UrgencyLevel, PageResult } from "@/features/request/types/request.types";
import type { Team } from "@/features/dispatch/types/dispatch.types";
import type { Vehicle } from "@/features/resource/types/resource.types";

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
      const request = requestsQuery.data?.content.find(r => r.id === selectedRequestId);
      if (!request) {
        throw new Error("Không tìm thấy thông tin yêu cầu");
      }

      return assignTeam({
        requestId: selectedRequestId,
        citizenId: request.citizenId,
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

      <section className="rounded-2xl border border-slate-200 bg-white p-4">
        <div className="mt-3 space-y-3">
          {requestsQuery.data?.content.map((item) => (
            <article key={item.id} className="rounded-xl border border-slate-200 p-3">
              <div className="flex items-center justify-between gap-2">
                <div>
                  <p className="font-semibold text-slate-900">Yêu cầu #{item.id}</p>
                  <p className="text-sm text-slate-600 line-clamp-1">{item.description}</p>
                </div>
                <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
                  {item.status}
                </span>
              </div>
              <div className="mt-2 flex flex-wrap gap-2">
                <button
                  disabled={item.status !== "PENDING" || verifyMutation.isPending}
                  onClick={() => verifyMutation.mutate(item.id)}
                  className="rounded-lg bg-cyan-700 px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-50"
                >
                  Xác minh
                </button>
                <button
                  onClick={() => setSelectedRequestId(item.id)}
                  className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700"
                >
                  Chọn để phân công
                </button>
              </div>
            </article>
          ))}
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
        <div className="mt-3 h-96 w-full">
          <RescueMap
            requests={requestsQuery.data?.content ?? []}
            teams={teamsQuery.data ?? []}
            vehicles={vehiclesQuery.data?.content ?? []}
            onRequestClick={(request: RescueRequestSummary) => {
              toast.info(`Yêu cầu #${request.id}: ${request.description}`);
            }}
            onTeamClick={(team: Team) => {
              toast.info(`Đội ${team.name}: ${team.status}`);
            }}
            onVehicleClick={(vehicle: Vehicle) => {
              toast.info(`Phương tiện ${vehicle.plateNumber}: ${vehicle.status}`);
            }}
          />
        </div>
      </section>
    </div>
  );
}
