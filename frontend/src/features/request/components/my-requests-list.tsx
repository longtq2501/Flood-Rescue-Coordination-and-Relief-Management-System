"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";

import { getMyRequests } from "@/features/request/services/request.service";

function formatRequestDate(value: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    timeZone: "UTC",
  }).format(new Date(value));
}

export function MyRequestsList() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["my-requests"],
    queryFn: () => getMyRequests(),
  });

  if (isLoading) {
    return <div className="rounded-2xl border border-slate-200 bg-white p-4">Dang tai du lieu...</div>;
  }

  if (isError) {
    return (
      <div className="rounded-2xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
        {error instanceof Error ? error.message : "Khong tai duoc danh sach"}
      </div>
    );
  }

  return (
    <section className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:p-5">
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-lg font-semibold text-slate-900">Yeu cau cua toi</h2>
        <span className="text-xs text-slate-500">{data?.content.length ?? 0} mục</span>
      </div>
      <div className="mt-4 space-y-3">
        {data?.content.length ? (
          data.content.map((item) => (
            <article
              key={item.id}
              className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 transition hover:border-teal-500 hover:bg-white"
            >
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div>
                  <p className="font-semibold text-slate-900">Request #{item.id}</p>
                  <p className="mt-1 line-clamp-2 text-sm leading-6 text-slate-600">{item.description}</p>
                </div>
                <span className="inline-flex w-fit rounded-full bg-white px-2.5 py-1 text-xs font-semibold text-slate-700 ring-1 ring-slate-200">
                  {item.status}
                </span>
              </div>

              <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                <span className="rounded-full bg-white px-2.5 py-1 ring-1 ring-slate-200">
                  Độ khẩn: {item.urgencyLevel}
                </span>
                <span className="rounded-full bg-white px-2.5 py-1 ring-1 ring-slate-200">
                  {formatRequestDate(item.createdAt)}
                </span>
              </div>

              <div className="mt-4 flex justify-end">
                <Link
                  href={`/dashboard/citizen/requests/${item.id}`}
                  className="font-semibold text-teal-700 hover:text-teal-800"
                >
                  Xem chi tiet
                </Link>
              </div>
            </article>
          ))
        ) : (
          <p className="text-sm text-slate-600">Ban chua co request nao.</p>
        )}
      </div>
    </section>
  );
}
