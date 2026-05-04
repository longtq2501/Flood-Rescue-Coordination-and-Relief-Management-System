"use client";

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { getWarehouses } from "@/features/resource/services/resource.service";
import { Button } from "@/components/ui/button";
import { Plus, Eye } from "lucide-react";

function formatNumber(n: number) {
  try {
    return new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 }).format(n);
  } catch (e) {
    return String(n);
  }
}

export function WarehouseList() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["warehouses"],
    queryFn: () => getWarehouses(),
  });

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500">Loading warehouses...</div>;
  }

  if (isError) {
    return (
      <div className="p-8 text-center text-red-500">
        Error: {error instanceof Error ? error.message : "Failed to load warehouses"}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Warehouses</h1>
          <p className="text-slate-500">Manage relief supply storage locations.</p>
        </div>
        <Link href="/dashboard/manager/warehouses/create">
          <Button icon={Plus}>Add Warehouse</Button>
        </Link>
      </div>

      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Name
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Location
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Capacity
                </th>
                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {data?.content?.map((warehouse) => (
                <tr key={warehouse.id} className="hover:bg-slate-50 transition-colors">
                  <td className="whitespace-nowrap px-6 py-4 font-medium text-slate-900">
                    {warehouse.name}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-slate-600">
                    {warehouse.location}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-slate-600">
                    <span className="rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700">
                      {formatNumber(warehouse.capacity)} units
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <Link href={`/dashboard/manager/warehouses/${warehouse.id}`}>
                      <Button variant="ghost" icon={Eye} className="text-brand-600">
                        View
                      </Button>
                    </Link>
                  </td>
                </tr>
              ))}
              {(!data?.content || data.content.length === 0) && (
                <tr>
                  <td colSpan={4} className="px-6 py-12 text-center text-slate-500">
                    No warehouses found. Click &quot;Add Warehouse&quot; to create one.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
