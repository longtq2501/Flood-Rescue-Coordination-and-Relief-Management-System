"use client";

import { useQuery } from "@tanstack/react-query";
import { CalendarClock, Package, MapPin, User } from "lucide-react";

import { Card } from "@/components/ui";
import { getDistributions, getDistributionsByRequest } from "@/features/resource/services/resource.service";
import type { Distribution } from "@/features/resource/types/resource.types";

type DistributionHistoryListProps = {
  requestId?: number;
  title?: string;
  description?: string;
};

function formatDistributedAt(value: string) {
  // Use a deterministic ISO-based format to avoid Intl differences between
  // server and client runtime which can cause hydration mismatches.
  try {
    const d = new Date(value);
    // Format as YYYY-MM-DD HH:MM (UTC)
    const iso = d.toISOString();
    return iso.replace('T', ' ').slice(0, 16);
  } catch (e) {
    return value;
  }
}

function DistributionCard({ distribution, requestScoped }: { distribution: Distribution; requestScoped: boolean }) {
  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm transition-shadow hover:shadow-md">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="text-base font-semibold text-slate-900">Phiếu phân phối #{distribution.id}</h3>
            {!requestScoped && (
              <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">
                Yêu cầu #{distribution.requestId}
              </span>
            )}
          </div>
          <div className="flex flex-wrap gap-3 text-sm text-slate-500">
            <span className="inline-flex items-center gap-1.5">
              <User className="h-4 w-4" />
              {distribution.recipientName}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <MapPin className="h-4 w-4" />
              Người nhận #{distribution.recipientId}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <CalendarClock className="h-4 w-4" />
              {formatDistributedAt(distribution.distributedAt)}
            </span>
          </div>
        </div>
      </div>

      <div className="mt-4 space-y-3">
        <div className="flex items-center gap-2 text-sm font-semibold text-slate-900">
          <Package className="h-4 w-4 text-teal-600" />
          Hàng đã cấp phát
        </div>
        <div className="flex flex-wrap gap-2">
          {distribution.items.map((item, index) => (
            <span
              key={`${distribution.id}-${item.reliefItemId}-${index}`}
              className="rounded-full border border-teal-200 bg-teal-50 px-3 py-1.5 text-xs font-semibold text-teal-800"
            >
              {item.itemName}: {item.quantity} {item.unit}
            </span>
          ))}
        </div>
        {distribution.note && (
          <p className="rounded-xl bg-slate-50 px-3 py-2 text-sm leading-6 text-slate-600">
            {distribution.note}
          </p>
        )}
      </div>
    </article>
  );
}

export function DistributionHistoryList({ requestId, title = "Lịch sử phân phối", description }: DistributionHistoryListProps) {
  const query = useQuery({
    queryKey: ["resource-distributions", requestId ?? "all"],
    queryFn: () => (requestId ? getDistributionsByRequest(requestId) : getDistributions({ page: 0, size: 20 })),
  });

  const distributions = query.data?.content ?? [];

  return (
    <Card className="space-y-4">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
        <p className="text-sm text-slate-500">
          {description ?? (requestId ? "Các lần cấp phát đã ghi nhận cho yêu cầu này" : "Tất cả các lần cấp phát gần đây")}
        </p>
      </div>

      {query.isLoading ? (
        <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          Đang tải lịch sử phân phối...
        </div>
      ) : query.isError ? (
        <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-4 text-sm text-red-700">
          {query.error instanceof Error ? query.error.message : "Không tải được lịch sử phân phối"}
        </div>
      ) : distributions.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
          Chưa có dữ liệu phân phối.
        </div>
      ) : (
        <div className="space-y-3">
          {distributions.map((distribution) => (
            <DistributionCard
              key={distribution.id}
              distribution={distribution}
              requestScoped={Boolean(requestId)}
            />
          ))}
        </div>
      )}
    </Card>
  );
}