"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import {
  completeAssignment,
  getMyAssignments,
  startAssignment,
} from "@/features/dispatch/services/dispatch.service";

export function RescueTeamBoard() {
  const queryClient = useQueryClient();
  const assignmentsQuery = useQuery({
    queryKey: ["my-assignments"],
    queryFn: getMyAssignments,
  });

  const startMutation = useMutation({
    mutationFn: (id: number) => startAssignment(id),
    onSuccess: () => {
      toast.success("Da bat dau nhiem vu");
      queryClient.invalidateQueries({ queryKey: ["my-assignments"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Start nhiem vu that bai");
    },
  });

  const completeMutation = useMutation({
    mutationFn: (id: number) => completeAssignment(id, "Da cuu ho thanh cong"),
    onSuccess: () => {
      toast.success("Da hoan tat nhiem vu");
      queryClient.invalidateQueries({ queryKey: ["my-assignments"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Complete nhiem vu that bai");
    },
  });

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-4">
      <h2 className="text-lg font-semibold text-slate-900">My assignments</h2>
      <div className="mt-3 space-y-3">
        {assignmentsQuery.data?.content.length ? (
          assignmentsQuery.data.content.map((item) => (
            <article key={item.id} className="rounded-xl border border-slate-200 p-3">
              <div className="flex items-center justify-between">
                <p className="font-semibold text-slate-900">Assignment #{item.id}</p>
                <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
                  {item.status}
                </span>
              </div>
              <p className="mt-1 text-sm text-slate-600">Request #{item.requestId}</p>
              <div className="mt-2 flex gap-2">
                <button
                  disabled={item.status !== "ACTIVE" || startMutation.isPending}
                  onClick={() => startMutation.mutate(item.id)}
                  className="rounded-lg bg-cyan-700 px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-50"
                >
                  Start
                </button>
                <button
                  disabled={completeMutation.isPending}
                  onClick={() => completeMutation.mutate(item.id)}
                  className="rounded-lg bg-emerald-700 px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-50"
                >
                  Complete
                </button>
              </div>
            </article>
          ))
        ) : (
          <p className="text-sm text-slate-600">Khong co assignment nao.</p>
        )}
      </div>
    </section>
  );
}
