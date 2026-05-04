"use client";

import { Send } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

import { DistributionForm } from "@/features/resource/components/distribution/DistributionForm";
import { DistributionHistoryList } from "@/features/resource/components/distribution/DistributionHistoryList";
import { getWarehouses, getItemsByWarehouse } from "@/features/resource/services/resource.service";
import { Card } from "@/components/ui";

export default function ManagerDistributionsPage() {
  const warehousesQuery = useQuery({
    queryKey: ["manager-distribution-warehouses"],
    queryFn: getWarehouses,
  });

  const firstWarehouseId = warehousesQuery.data?.content?.[0]?.id;

  const itemsQuery = useQuery({
    queryKey: ["manager-distribution-items", firstWarehouseId],
    queryFn: () => getItemsByWarehouse(Number(firstWarehouseId)),
    enabled: Boolean(firstWarehouseId),
  });

  const warehouseCount = warehousesQuery.data?.content?.length ?? 0;
  const itemCount = itemsQuery.data?.content?.length ?? 0;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="rounded-2xl bg-teal-600 p-3 text-white shadow-lg shadow-teal-100">
          <Send className="h-6 w-6" />
        </div>
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Phân phối cứu trợ</h1>
          <p className="text-slate-500">Ghi nhận cấp phát hàng từ kho đến từng yêu cầu cứu trợ</p>
        </div>
      </div>

      <section className="grid gap-4 md:grid-cols-3">
        <Card className="p-5">
          <p className="text-sm text-slate-500">Số kho khả dụng</p>
          <p className="mt-2 text-3xl font-bold text-slate-900">{warehouseCount}</p>
          <p className="mt-2 text-sm text-slate-500">Nguồn hàng để xuất phân phối.</p>
        </Card>
        <Card className="p-5">
          <p className="text-sm text-slate-500">Mặt hàng kho đầu tiên</p>
          <p className="mt-2 text-3xl font-bold text-slate-900">{itemCount}</p>
          <p className="mt-2 text-sm text-slate-500">Dữ liệu live từ kho đang chọn mặc định.</p>
        </Card>
        <Card className="p-5">
          <p className="text-sm text-slate-500">Trạng thái demo</p>
          <p className="mt-2 text-3xl font-bold text-slate-900">Sẵn sàng</p>
          <p className="mt-2 text-sm text-slate-500">Khi có dữ liệu thật, lịch sử sẽ hiện ra ngay dưới đây.</p>
        </Card>
      </section>

      <DistributionForm title="Tạo bản ghi phân phối" />
      <DistributionHistoryList title="Lịch sử phân phối gần đây" />
    </div>
  );
}