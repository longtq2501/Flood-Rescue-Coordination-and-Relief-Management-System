"use client";

import { useState, useEffect } from "react";
import { getWarehouses } from "@/features/resource/services/resource.service";
import type { Warehouse } from "@/features/resource/types/warehouse.types";
import { InventoryList } from "@/features/resource/components/inventory/InventoryList";
import { AddItemForm } from "@/features/resource/components/inventory/AddItemForm";
import { Modal } from "@/components/ui/modal";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { 
  Warehouse as WarehouseIcon, 
  Plus, 
  MapPin, 
  LayoutGrid,
  ClipboardList
} from "lucide-react";
import { toast } from "sonner";

export default function InventoryPage() {
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [isAddItemModalOpen, setIsAddItemModalOpen] = useState(false);

  useEffect(() => {
    const fetchWarehouses = async () => {
      try {
        const data = await getWarehouses();
        const content = data?.content ?? [];
        setWarehouses(content);
        if (content.length > 0) setSelectedWarehouseId(content[0].id);
      } catch (error: any) {
        toast.error(error.message || "Không thể tải danh sách kho");
      } finally {
        setLoading(false);
      }
    };
    fetchWarehouses();
  }, []);

  const selectedWarehouse = warehouses.find(w => w.id === selectedWarehouseId);

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-2xl bg-brand-600 text-white shadow-lg shadow-brand-100">
            <ClipboardList className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Quản lý tồn kho</h1>
            <p className="text-slate-500">Theo dõi nhu yếu phẩm và hàng cứu trợ tại các kho</p>
          </div>
        </div>

        {selectedWarehouseId && (
          <Button icon={Plus} onClick={() => setIsAddItemModalOpen(true)}>
            Thêm hàng mới
          </Button>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <div className="lg:col-span-1 space-y-4">
          <h3 className="text-sm font-bold text-slate-400 uppercase tracking-wider px-1">Danh sách kho</h3>
          <div className="space-y-2">
            {loading ? (
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-20 w-full animate-pulse bg-slate-100 rounded-xl" />
              ))
            ) : warehouses.map((warehouse) => (
              <button
                key={warehouse.id}
                onClick={() => setSelectedWarehouseId(warehouse.id)}
                className={`w-full text-left p-4 rounded-xl border transition-all ${
                  selectedWarehouseId === warehouse.id
                    ? 'border-brand-500 bg-brand-50/50 ring-1 ring-brand-500 shadow-sm'
                    : 'border-slate-100 bg-white hover:border-slate-200 hover:bg-slate-50'
                }`}
              >
                <div className="flex items-start gap-3">
                  <div className={`p-2 rounded-lg ${selectedWarehouseId === warehouse.id ? 'bg-brand-500 text-white' : 'bg-slate-100 text-slate-500'}`}>
                    <WarehouseIcon className="h-4 w-4" />
                  </div>
                  <div>
                    <p className="font-bold text-slate-900 text-sm">{warehouse.name}</p>
                    <div className="flex items-center gap-1 text-xs text-slate-500 mt-1">
                      <MapPin className="h-3 w-3" />
                      <span className="line-clamp-1">{warehouse.location}</span>
                    </div>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        <div className="lg:col-span-3">
          {selectedWarehouseId ? (
            <div className="space-y-6">
              <Card className="bg-gradient-to-r from-brand-600 to-brand-700 text-white border-none shadow-brand-100 shadow-xl p-6">
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-2xl font-bold">{selectedWarehouse?.name}</h2>
                    <p className="flex items-center gap-1 text-brand-100 text-sm mt-1">
                      <MapPin className="h-4 w-4" />
                      {selectedWarehouse?.location}
                    </p>
                  </div>
                  <div className="bg-white/20 p-3 rounded-xl backdrop-blur-md">
                    <LayoutGrid className="h-6 w-6" />
                  </div>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8">
                  <div className="bg-white/10 p-3 rounded-lg backdrop-blur-sm">
                    <p className="text-xs text-brand-100 uppercase font-bold">Mã kho</p>
                    <p className="text-lg font-bold">#WH-{selectedWarehouseId}</p>
                  </div>
                  <div className="bg-white/10 p-3 rounded-lg backdrop-blur-sm">
                    <p className="text-xs text-brand-100 uppercase font-bold">Loại kho</p>
                    <p className="text-lg font-bold">Tổng hợp</p>
                  </div>
                </div>
              </Card>

              <InventoryList warehouseId={selectedWarehouseId} />
            </div>
          ) : (
            <Card className="h-[400px] flex flex-col items-center justify-center text-slate-500 border-dashed">
              <WarehouseIcon className="h-12 w-12 mb-4 opacity-20" />
              <p>Vui lòng chọn một kho để xem tồn kho</p>
            </Card>
          )}
        </div>
      </div>

      <Modal
        open={isAddItemModalOpen}
        onClose={() => setIsAddItemModalOpen(false)}
        title={`Thêm hàng hóa vào ${selectedWarehouse?.name}`}
      >
        {selectedWarehouseId && (
          <AddItemForm 
            warehouseId={selectedWarehouseId}
            onSuccess={() => {
              setIsAddItemModalOpen(false);
              window.location.reload();
            }}
            onCancel={() => setIsAddItemModalOpen(false)}
          />
        )}
      </Modal>
    </div>
  );
}
