"use client";

import * as React from "react";
import { getItemsByWarehouse } from "@/features/resource/services/resource.service";
import { type ReliefItem } from "@/features/resource/types/resource.types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { 
  Package, 
  AlertTriangle, 
  RefreshCw
} from "lucide-react";
import { toast } from "sonner";
import { UpdateStockModal } from "@/features/resource/components/inventory/UpdateStockModal";

interface InventoryListProps {
  warehouseId: number | null;
}

export function InventoryList({ warehouseId }: InventoryListProps) {
  const [items, setItems] = React.useState<ReliefItem[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [selectedItem, setSelectedItem] = React.useState<ReliefItem | null>(null);

  const fetchData = React.useCallback(async () => {
    if (warehouseId === null || warehouseId === undefined) return;
    
    setLoading(true);
    try {
      const data = await getItemsByWarehouse(warehouseId);
      setItems(data?.content || []);
    } catch (error: unknown) {
      console.error("Fetch inventory error:", error);
      const message = error instanceof Error ? error.message : "Không thể tải danh sách hàng hóa";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  }, [warehouseId]);

  React.useEffect(() => {
    if (warehouseId !== null && warehouseId !== undefined) {
      fetchData();
    } else {
      setItems([]);
    }
  }, [warehouseId, fetchData]);

  if (warehouseId === null || warehouseId === undefined) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-slate-400 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
        <Package className="h-8 w-8 mb-2 opacity-20" />
        <p className="text-sm font-medium">Vui lòng chọn một kho để xem hàng hóa</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="p-2 rounded-lg bg-brand-50 text-brand-600">
            <Package className="h-5 w-5" />
          </div>
          <h3 className="text-lg font-bold text-slate-900 tracking-tight">Danh sách hàng hóa</h3>
        </div>
        <Button 
          variant="ghost" 
          onClick={fetchData} 
          disabled={loading}
          className="h-10 w-10 p-0 flex items-center justify-center rounded-full hover:bg-slate-100"
        >
          <RefreshCw className={`h-4 w-4 text-slate-500 ${loading ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="bg-slate-50/80 border-b border-slate-200">
                <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Tên hàng hóa</th>
                <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Phân loại</th>
                <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Số lượng</th>
                <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider">Trạng thái</th>
                <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {loading && items.length === 0 ? (
                [1, 2, 3].map((i) => (
                  <tr key={i}>
                    <td colSpan={5} className="px-6 py-10">
                      <div className="h-4 bg-slate-100 animate-pulse rounded-full w-full" />
                    </td>
                  </tr>
                ))
              ) : items.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-16 text-center text-slate-400 italic">
                    <Package className="h-10 w-10 mx-auto mb-2 opacity-10" />
                    <p>Kho này hiện chưa có dữ liệu hàng hóa</p>
                  </td>
                </tr>
              ) : (
                items.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/50 transition-colors group">
                    <td className="px-6 py-4">
                      <p className="font-bold text-slate-900">{item.name}</p>
                    </td>
                    <td className="px-6 py-4">
                      <Badge className="bg-slate-100 text-slate-600 font-bold border-none">
                        {item.category || "Nhu yếu phẩm"}
                      </Badge>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-baseline gap-1">
                        <span className="font-black text-slate-900 text-base">{item.quantity}</span>
                        <span className="text-[10px] text-slate-400 font-bold uppercase">{item.unit}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {item.belowThreshold ? (
                        <div className="inline-flex items-center gap-1.5 text-[10px] font-black text-orange-600 bg-orange-50 border border-orange-100 px-2.5 py-1 rounded-full">
                          <AlertTriangle className="h-3 w-3" />
                          SẮP HẾT
                        </div>
                      ) : (
                        <div className="inline-flex items-center gap-1.5 text-[10px] font-black text-emerald-600 bg-emerald-50 border border-emerald-100 px-2.5 py-1 rounded-full">
                          ỔN ĐỊNH
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Button 
                        variant="ghost" 
                        className="text-brand-600 hover:text-brand-700 hover:bg-brand-50 h-8 px-4 text-xs font-black rounded-full"
                        onClick={() => setSelectedItem(item)}
                      >
                        Cập nhật
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {selectedItem && (
        <UpdateStockModal 
          item={selectedItem}
          open={!!selectedItem}
          onClose={() => setSelectedItem(null)}
          onSuccess={() => {
            setSelectedItem(null);
            fetchData();
          }}
        />
      )}
    </div>
  );
}
