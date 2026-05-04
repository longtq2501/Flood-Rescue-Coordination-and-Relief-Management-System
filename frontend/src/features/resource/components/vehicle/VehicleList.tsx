"use client";

import { useState, useEffect } from "react";
import {
  getVehicles,
  updateVehicleStatus
} from "../../services/resource.service";
import type { Vehicle, VehicleStatus, VehicleType } from "../../types";
import { VehicleStatusBadge } from "./VehicleStatusBadge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Truck,
  Ship,
  Plane,
  Activity,
  MoreVertical,
  RefreshCw,
  Search,
  Plus
} from "lucide-react";
import { toast } from "sonner";
import { Modal } from "@/components/ui/modal";
import { AddVehicleForm } from "./AddVehicleForm";
import clsx from "clsx";

const typeIcons: Record<VehicleType, React.ComponentType<{ className?: string }>> = {
  BOAT: Ship,
  TRUCK: Truck,
  HELICOPTER: Plane,
  AMBULANCE: Activity,
  OTHER: Truck,
};

export function VehicleList() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [filterStatus, setFilterStatus] = useState<VehicleStatus | "">("");
  const [filterType, setFilterType] = useState<VehicleType | "">("");

  const fetchData = async () => {
    setLoading(true);
    try {
      const data = await getVehicles();
      setVehicles(data.content);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Không thể tải danh sách phương tiện";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [filterStatus, filterType]);

  const handleStatusChange = async (id: number, newStatus: VehicleStatus) => {
    try {
      await updateVehicleStatus(id, newStatus);
      toast.success("Cập nhật trạng thái thành công");
      fetchData();
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Không thể cập nhật trạng thái";
      toast.error(message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Tìm kiếm biển số..."
              className="pl-9 pr-4 py-2 text-sm border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-brand-500 w-64"
            />
          </div>

          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value as VehicleStatus)}
            className="px-3 py-2 text-sm border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="AVAILABLE">Sẵn sàng</option>
            <option value="IN_USE">Đang sử dụng</option>
            <option value="MAINTENANCE">Bảo trì</option>
            <option value="OFFLINE">Ngoại tuyến</option>
          </select>

          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value as VehicleType)}
            className="px-3 py-2 text-sm border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-brand-500 bg-white"
          >
            <option value="">Tất cả loại xe</option>
            <option value="BOAT">Cano / Thuyền</option>
            <option value="TRUCK">Xe tải</option>
            <option value="HELICOPTER">Trực thăng</option>
            <option value="AMBULANCE">Xe cấp cứu</option>
          </select>

          <Button variant="ghost" onClick={fetchData} disabled={loading} className="p-2">
            <RefreshCw className={clsx("h-4 w-4", loading && "animate-spin")} />
          </Button>
        </div>

        <Button onClick={() => setIsAddModalOpen(true)} icon={Plus}>
          Thêm phương tiện
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {loading && vehicles.length === 0 ? (
          Array.from({ length: 6 }).map((_, i) => (
            <Card key={i} className="animate-pulse h-40 bg-slate-50" children={undefined} />
          ))
        ) : vehicles.length === 0 ? (
          <div className="col-span-full py-20 text-center text-slate-500 bg-white rounded-xl border border-dashed border-slate-300">
            Không tìm thấy phương tiện nào khớp với bộ lọc
          </div>
        ) : (
          vehicles.map((vehicle) => {
            const Icon = typeIcons[vehicle.type] || Truck;
            return (
              <Card key={vehicle.id} className="hover:shadow-md transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-brand-50 text-brand-600">
                      <Icon className="h-5 w-5" />
                    </div>
                    <div>
                      <h4 className="font-bold text-slate-900">{vehicle.plateNumber}</h4>
                      <p className="text-xs text-slate-500 capitalize">{vehicle.type.toLowerCase()}</p>
                    </div>
                  </div>
                  <VehicleStatusBadge status={vehicle.status} />
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Sức chứa:</span>
                    <span className="font-medium">{vehicle.capacity} người</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500">Vị trí hiện tại:</span>
                    <span className="font-medium text-xs text-slate-400">
                      {vehicle.currentLat?.toFixed(4)}, {vehicle.currentLng?.toFixed(4)}
                    </span>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2 border-t">
                  <select
                    className="flex-1 text-xs border border-slate-200 rounded px-2 py-1 bg-white focus:outline-none focus:ring-1 focus:ring-brand-500"
                    value={vehicle.status}
                    onChange={(e) => handleStatusChange(vehicle.id, e.target.value as VehicleStatus)}
                  >
                    <option value="AVAILABLE">Sẵn sàng</option>
                    <option value="IN_USE">Đang sử dụng</option>
                    <option value="MAINTENANCE">Bảo trì</option>
                    <option value="OFFLINE">Ngoại tuyến</option>
                  </select>
                  <Button variant="ghost" className="h-8 w-8 p-0" icon={MoreVertical}>
                    <span className="sr-only">Thêm</span>
                  </Button>
                </div>
              </Card>
            );
          })
        )}
      </div>

      <Modal
        open={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        title="Thêm phương tiện cứu hộ mới"
      >
        <AddVehicleForm
          onSuccess={() => {
            setIsAddModalOpen(false);
            fetchData();
          }}
          onCancel={() => setIsAddModalOpen(false)}
        />
      </Modal>
    </div>
  );
}
