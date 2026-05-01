"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import {
  cancelRequest,
  confirmRequest,
  getRequestDetail,
} from "@/features/request/services/request.service";

export function RequestDetailCard({ id }: { id: string }) {
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ["request-detail", id],
    queryFn: () => getRequestDetail(id),
  });

  const cancelMutation = useMutation({
    mutationFn: () => cancelRequest(id, "Huy tu phia citizen"),
    onSuccess: () => {
      toast.success("Da huy request");
      queryClient.invalidateQueries({ queryKey: ["request-detail", id] });
      queryClient.invalidateQueries({ queryKey: ["my-requests"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Khong the huy request");
    },
  });

  const confirmMutation = useMutation({
    mutationFn: () => confirmRequest(id, "Da duoc cuu ho"),
    onSuccess: () => {
      toast.success("Da xac nhan da duoc cuu ho");
      queryClient.invalidateQueries({ queryKey: ["request-detail", id] });
      queryClient.invalidateQueries({ queryKey: ["my-requests"] });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Khong the xac nhan request");
    },
  });

  if (query.isLoading) {
    return <div className="rounded-2xl border border-slate-200 bg-white p-4">Dang tai request...</div>;
  }

  if (query.isError || !query.data) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
        {query.error instanceof Error ? query.error.message : "Khong tai duoc request"}
      </div>
    );
  }

  const { data } = query;
  const canCancel = data.status === "PENDING";
  const canConfirm = data.status === "COMPLETED";

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-4">
      <h1 className="text-xl font-bold text-slate-900">Request #{data.id}</h1>
      <p className="mt-1 text-sm text-slate-600">{data.description}</p>

      <div className="mt-4 grid gap-2 text-sm md:grid-cols-2">
        <p>
          <span className="font-semibold">Status:</span> {data.status}
        </p>
        <p>
          <span className="font-semibold">Urgency:</span> {data.urgencyLevel}
        </p>
        <p>
          <span className="font-semibold">Nguoi can cuu:</span> {data.numPeople}
        </p>
        <p>
          <span className="font-semibold">Dia chi:</span> {data.addressText || "Khong co"}
        </p>
      </div>

      <div className="mt-4 flex flex-wrap gap-2">
        <button
          disabled={!canCancel || cancelMutation.isPending}
          onClick={() => cancelMutation.mutate()}
          className="rounded-lg bg-red-600 px-3 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
        >
          Huy request
        </button>
        <button
          disabled={!canConfirm || confirmMutation.isPending}
          onClick={() => confirmMutation.mutate()}
          className="rounded-lg bg-emerald-600 px-3 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
        >
          Xac nhan da duoc cuu
        </button>
      </div>
    </section>
  );
}
