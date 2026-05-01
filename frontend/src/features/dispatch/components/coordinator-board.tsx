"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import {
  assignTeam,
  getTeams,
} from "@/features/dispatch/services/dispatch.service";
import {
  fetchCoordinatorRequests,
  verifyRequest,
} from "@/features/request/services/request.service";
import { getVehicles } from "@/features/resource/services/resource.service";

export function CoordinatorBoard() {
  const queryClient = useQueryClient();
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null);
  const [selectedVehicleId, setSelectedVehicleId] = useState<number | null>(null);

  const requestsQuery = useQuery({
    queryKey: ["coordinator-requests"],
    queryFn: fetchCoordinatorRequests,
  });

  const teamsQuery = useQuery({
    queryKey: ["dispatch-teams"],
    queryFn: getTeams,
  });

  const vehiclesQuery = useQuery({
    queryKey: ["resource-vehicles"],
    queryFn: getVehicles,
  });

  const verifyMutation = useMutation({
    mutationFn: (id: number) => verifyRequest(String(id), "Verified from coordinator board"),
    onSuccess: () => {
      toast.success("Verify request thanh cong");
      queryClient.invalidateQueries({ queryKey: ["coordinator-requests"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Verify request that bai");
    },
  });

  const assignMutation = useMutation({
    mutationFn: () => {
      if (!selectedRequestId || !selectedTeamId || !selectedVehicleId) {
        throw new Error("Can chon request, team va vehicle");
      }
      return assignTeam({
        requestId: selectedRequestId,
        teamId: selectedTeamId,
        vehicleId: selectedVehicleId,
        note: "Demo assign",
      });
    },
    onSuccess: () => {
      toast.success("Assign team thanh cong");
      queryClient.invalidateQueries({ queryKey: ["coordinator-requests"] });
      setSelectedRequestId(null);
      setSelectedTeamId(null);
      setSelectedVehicleId(null);
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Assign team that bai");
    },
  });

  const verifiedRequests = useMemo(
    () => requestsQuery.data?.content.filter((item) => item.status === "VERIFIED") ?? [],
    [requestsQuery.data],
  );

  return (
    <div className="space-y-4">
      <section className="rounded-2xl border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-slate-900">Request verification queue</h2>
        <div className="mt-3 space-y-3">
          {requestsQuery.data?.content.map((item) => (
            <article key={item.id} className="rounded-xl border border-slate-200 p-3">
              <div className="flex items-center justify-between gap-2">
                <div>
                  <p className="font-semibold text-slate-900">Request #{item.id}</p>
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
                  Verify
                </button>
                <button
                  onClick={() => setSelectedRequestId(item.id)}
                  className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700"
                >
                  Chon de assign
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="rounded-2xl border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-slate-900">Assign panel</h2>
        <p className="mt-1 text-sm text-slate-600">Chi assign request da VERIFIED.</p>
        <div className="mt-3 grid gap-3 md:grid-cols-3">
          <select
            className="rounded-xl border border-slate-300 px-3 py-2"
            value={selectedRequestId ?? ""}
            onChange={(event) => setSelectedRequestId(Number(event.target.value) || null)}
          >
            <option value="">Select request</option>
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
            <option value="">Select team</option>
            {teamsQuery.data?.content.map((item) => (
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
            <option value="">Select vehicle</option>
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
          {assignMutation.isPending ? "Dang assign..." : "Assign ngay"}
        </button>
      </section>
    </div>
  );
}
