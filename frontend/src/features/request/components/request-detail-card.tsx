"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { toast } from "sonner";
import Image from "next/image";

import { DistributionForm } from "@/features/resource/components/distribution/DistributionForm";
import { DistributionHistoryList } from "@/features/resource/components/distribution/DistributionHistoryList";

import {
  cancelRequest,
  confirmRequest,
  getRequestDetail,
} from "@/features/request/services/request.service";

const statusStyles: Record<string, string> = {
  PENDING: "bg-amber-50 text-amber-700 ring-1 ring-amber-200",
  VERIFIED: "bg-sky-50 text-sky-700 ring-1 ring-sky-200",
  ASSIGNED: "bg-violet-50 text-violet-700 ring-1 ring-violet-200",
  COMPLETED: "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200",
  CONFIRMED: "bg-teal-50 text-teal-700 ring-1 ring-teal-200",
  CANCELLED: "bg-slate-100 text-slate-600 ring-1 ring-slate-200",
};

const urgencyStyles: Record<string, string> = {
  CRITICAL: "bg-rose-50 text-rose-700 ring-1 ring-rose-200",
  HIGH: "bg-orange-50 text-orange-700 ring-1 ring-orange-200",
  MEDIUM: "bg-amber-50 text-amber-700 ring-1 ring-amber-200",
  LOW: "bg-slate-100 text-slate-600 ring-1 ring-slate-200",
};

export function RequestDetailCard({ id }: { id: string }) {
  const queryClient = useQueryClient();
  const role = useAuthStore((state) => state.role);
  const hydrated = useAuthStore((state) => state.hydrated);
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
  const canManageDistribution = hydrated && (role === "COORDINATOR" || role === "MANAGER" || role === "ADMIN");

  return (
    <section className="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
      <div className="border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white px-4 py-4 sm:px-6">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-slate-900">Request #{data.id}</h1>
              <span className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${statusStyles[data.status] ?? "bg-slate-100 text-slate-700 ring-1 ring-slate-200"}`}>
                {data.status}
              </span>
              <span className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${urgencyStyles[data.urgencyLevel] ?? "bg-slate-100 text-slate-700 ring-1 ring-slate-200"}`}>
                {data.urgencyLevel}
              </span>
            </div>
            <p className="max-w-3xl text-sm leading-6 text-slate-600 sm:text-base">
              {data.description}
            </p>
          </div>
        </div>
      </div>

      <div className="grid gap-6 px-4 py-5 sm:px-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Số người cần cứu</p>
              <p className="mt-1 text-lg font-bold text-slate-900">{data.numPeople}</p>
            </div>
            <div className="rounded-2xl bg-slate-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Địa chỉ</p>
              <p className="mt-1 text-sm font-medium leading-6 text-slate-900">
                {data.addressText || "Không có địa chỉ chi tiết"}
              </p>
            </div>
          </div>

          {data.imageUrls && data.imageUrls.length > 0 && (
            <div>
              <div className="mb-3 flex items-center justify-between gap-3">
                <h3 className="text-sm font-semibold text-slate-900">Hình ảnh hiện trường</h3>
                <span className="text-xs text-slate-500">{data.imageUrls.length} ảnh</span>
              </div>
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                {data.imageUrls.map((url, i) => (
                  <a
                    key={i}
                    href={url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="group relative aspect-square overflow-hidden rounded-2xl border border-slate-200 bg-slate-100 shadow-sm transition-transform hover:-translate-y-0.5 hover:shadow-md"
                  >
                    <Image
                      src={url}
                      alt={`Hiện trường ${i + 1}`}
                      fill
                      sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 240px"
                      className="object-cover transition-transform duration-200 group-hover:scale-105"
                      unoptimized
                    />
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>

        <aside className="space-y-3 rounded-2xl border border-slate-200 bg-slate-50 p-4">
          <h3 className="text-sm font-semibold text-slate-900">Tóm tắt</h3>
          <dl className="space-y-3 text-sm">
            <div className="flex items-start justify-between gap-4">
              <dt className="text-slate-500">Trạng thái</dt>
              <dd className="text-right font-semibold text-slate-900">{data.status}</dd>
            </div>
            <div className="flex items-start justify-between gap-4">
              <dt className="text-slate-500">Mức độ</dt>
              <dd className="text-right font-semibold text-slate-900">{data.urgencyLevel}</dd>
            </div>
            <div className="flex items-start justify-between gap-4">
              <dt className="text-slate-500">Số người</dt>
              <dd className="text-right font-semibold text-slate-900">{data.numPeople}</dd>
            </div>
            <div className="flex items-start justify-between gap-4">
              <dt className="text-slate-500">Tọa độ</dt>
              <dd className="text-right font-semibold text-slate-900">
                {data.lat.toFixed(5)}, {data.lng.toFixed(5)}
              </dd>
            </div>
          </dl>
        </aside>
      </div>

      <div className="border-t border-slate-100 px-4 py-4 sm:px-6">
        <div className="flex flex-wrap gap-2">
        {canCancel && (
          <button
            disabled={cancelMutation.isPending}
            onClick={() => cancelMutation.mutate()}
            className="rounded-xl bg-red-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Huy request
          </button>
        )}
        {canConfirm && (
          <button
            disabled={confirmMutation.isPending}
            onClick={() => confirmMutation.mutate()}
            className="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Xac nhan da duoc cuu
          </button>
        )}
        </div>
      </div>

      <div className="border-t border-slate-100 bg-slate-50/70 px-4 py-5 sm:px-6">
        <div className="space-y-5">
          <DistributionHistoryList
            requestId={Number(id)}
            title="Lịch sử phân phối cho yêu cầu này"
            description="Theo dõi các lần cấp phát hàng cứu trợ đã được ghi nhận"
          />

          {canManageDistribution && (
            <DistributionForm
              defaultRequestId={Number(id)}
              defaultRecipientId={data.citizenId}
              defaultRecipientName={data.citizenName || data.citizenPhone || "Người nhận"}
              title="Phân phối cứu trợ cho yêu cầu"
              onSuccess={() => {
                queryClient.invalidateQueries({ queryKey: ["resource-distributions"] });
              }}
            />
          )}
        </div>
      </div>
    </section>
  );
}
