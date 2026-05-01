"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";

import { getMyRequests } from "@/features/request/services/request.service";

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
    <section className="rounded-2xl border border-slate-200 bg-white p-4">
      <h2 className="text-lg font-semibold text-slate-900">Yeu cau cua toi</h2>
      <div className="mt-3 space-y-3">
        {data?.content.length ? (
          data.content.map((item) => (
            <article
              key={item.id}
              className="rounded-xl border border-slate-200 px-4 py-3 transition hover:border-teal-500"
            >
              <div className="flex items-center justify-between gap-2">
                <p className="font-semibold text-slate-900">Request #{item.id}</p>
                <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
                  {item.status}
                </span>
              </div>
              <p className="mt-1 text-sm text-slate-600 line-clamp-2">{item.description}</p>
              <div className="mt-2 flex items-center justify-between text-sm">
                <span className="text-slate-500">Urgency: {item.urgencyLevel}</span>
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
