"use client";

import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button, Card, Input } from "@/components/ui";
import { createDistribution, getItemsByWarehouse, getWarehouses } from "@/features/resource/services/resource.service";
import type { CreateDistributionRequest, CreateDistributionItemRequest } from "@/features/resource/types/resource.types";

type DistributionFormProps = {
  defaultRequestId?: number;
  defaultRecipientId?: number;
  defaultRecipientName?: string;
  onSuccess?: () => void;
  title?: string;
};

type SelectedDistributionItem = CreateDistributionItemRequest & {
  itemName: string;
  unit: string;
  stock: number;
};

export function DistributionForm({
  defaultRequestId,
  defaultRecipientId,
  defaultRecipientName,
  onSuccess,
  title = "Ghi nhận phân phối cứu trợ",
}: DistributionFormProps) {
  const queryClient = useQueryClient();
  const [requestId, setRequestId] = useState(defaultRequestId ? String(defaultRequestId) : "");
  const [recipientId, setRecipientId] = useState(defaultRecipientId ? String(defaultRecipientId) : "");
  const [selectedWarehouseId, setSelectedWarehouseId] = useState("");
  const [selectedItemId, setSelectedItemId] = useState("");
  const [selectedQuantity, setSelectedQuantity] = useState("1");
  const [selectedItems, setSelectedItems] = useState<SelectedDistributionItem[]>([]);
  const [note, setNote] = useState("");

  const warehousesQuery = useQuery({
    queryKey: ["resource-warehouses"],
    queryFn: getWarehouses,
  });

  const itemsQuery = useQuery({
    queryKey: ["resource-items", selectedWarehouseId],
    queryFn: () => getItemsByWarehouse(Number(selectedWarehouseId)),
    enabled: Boolean(selectedWarehouseId),
  });

  const selectedWarehouse = useMemo(
    () => warehousesQuery.data?.content.find((warehouse) => String(warehouse.id) === selectedWarehouseId),
    [selectedWarehouseId, warehousesQuery.data],
  );

  const availableItems = itemsQuery.data?.content ?? [];

  const createMutation = useMutation({
    mutationFn: async () => {
      if (!requestId || !recipientId || selectedItems.length === 0) {
        throw new Error("Vui lòng nhập yêu cầu, người nhận và ít nhất một mặt hàng");
      }

      const payload: CreateDistributionRequest = {
        requestId: Number(requestId),
        recipientId: Number(recipientId),
        items: selectedItems.map((item) => ({
          reliefItemId: item.reliefItemId,
          quantity: item.quantity,
        })),
        note: note.trim() || undefined,
      };

      return createDistribution(payload);
    },
    onSuccess: () => {
      toast.success("Đã ghi nhận phân phối cứu trợ");
      setNote("");
      setSelectedItems([]);
      queryClient.invalidateQueries({ queryKey: ["resource-distributions"] });
      onSuccess?.();
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Không thể tạo phân phối");
    },
  });

  const addItem = () => {
    const itemId = Number(selectedItemId);
    const quantity = Number(selectedQuantity);
    const selectedItem = availableItems.find((item) => item.id === itemId);

    if (!selectedItem) {
      toast.error("Hãy chọn một mặt hàng");
      return;
    }

    if (!Number.isFinite(quantity) || quantity <= 0) {
      toast.error("Số lượng phải lớn hơn 0");
      return;
    }

    if (quantity > selectedItem.quantity) {
      toast.error("Số lượng vượt tồn kho hiện tại");
      return;
    }

    if (selectedItems.some((item) => item.reliefItemId === selectedItem.id)) {
      toast.error("Mặt hàng này đã được thêm vào danh sách");
      return;
    }

    setSelectedItems((current) => [
      ...current,
      {
        reliefItemId: selectedItem.id,
        itemName: selectedItem.name,
        quantity,
        unit: selectedItem.unit,
        stock: selectedItem.quantity,
      },
    ]);
    setSelectedItemId("");
    setSelectedQuantity("1");
  };

  const removeItem = (reliefItemId: number) => {
    setSelectedItems((current) => current.filter((item) => item.reliefItemId !== reliefItemId));
  };

  const requestIsLocked = Boolean(defaultRequestId);
  const recipientIsLocked = Boolean(defaultRecipientId);

  return (
    <Card className="space-y-5">
      <div>
        <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
        <p className="text-sm text-slate-500">
          Ghi nhận mặt hàng, số lượng và người nhận để lưu lịch sử phân phối.
        </p>
      </div>

      {requestIsLocked || recipientIsLocked ? (
        <div className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 sm:grid-cols-2">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Yêu cầu</p>
            <p className="mt-1 font-semibold text-slate-900">#{defaultRequestId}</p>
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Người nhận</p>
            <p className="mt-1 font-semibold text-slate-900">
              {defaultRecipientName ? `${defaultRecipientName} (#${defaultRecipientId})` : `#${defaultRecipientId}`}
            </p>
          </div>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Mã yêu cầu</label>
            <Input
              type="number"
              min="1"
              value={requestId}
              onChange={(event) => setRequestId(event.target.value)}
              placeholder="Ví dụ: 1024"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium text-slate-700">Mã người nhận</label>
            <Input
              type="number"
              min="1"
              value={recipientId}
              onChange={(event) => setRecipientId(event.target.value)}
              placeholder="Mã người dùng của người dân"
            />
          </div>
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-[0.95fr_1.05fr]">
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Kho xuất hàng</label>
          <select
            value={selectedWarehouseId}
            onChange={(event) => {
              setSelectedWarehouseId(event.target.value);
              setSelectedItemId("");
              setSelectedItems([]);
            }}
            className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-900"
          >
            <option value="">Chọn kho hàng</option>
            {warehousesQuery.data?.content.map((warehouse) => (
              <option key={warehouse.id} value={warehouse.id}>
                {warehouse.name} - {warehouse.location}
              </option>
            ))}
          </select>
          <p className="text-xs text-slate-500">
            {selectedWarehouse ? `${selectedWarehouse.name} đang được chọn` : "Chọn kho trước để xem hàng hóa khả dụng"}
          </p>
        </div>

        <div className="space-y-3 rounded-2xl border border-slate-200 bg-slate-50 p-4">
          <div className="grid gap-3 sm:grid-cols-[1.5fr_0.7fr_auto]">
            <select
              value={selectedItemId}
              onChange={(event) => setSelectedItemId(event.target.value)}
              disabled={!selectedWarehouseId || itemsQuery.isLoading}
              className="h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-900 disabled:bg-slate-100"
            >
              <option value="">Chọn mặt hàng</option>
              {availableItems.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name} ({item.quantity} {item.unit})
                </option>
              ))}
            </select>
            <Input
              type="number"
              min="1"
              value={selectedQuantity}
              onChange={(event) => setSelectedQuantity(event.target.value)}
              placeholder="Số lượng"
              disabled={!selectedWarehouseId}
            />
            <Button type="button" onClick={addItem} variant="ghost" className="h-11 justify-center">
              <Plus className="h-4 w-4" />
              Thêm
            </Button>
          </div>

          {itemsQuery.isLoading ? (
            <p className="text-sm text-slate-500">Đang tải danh sách hàng hóa...</p>
          ) : selectedWarehouseId && availableItems.length === 0 ? (
            <p className="text-sm text-slate-500">Kho này chưa có mặt hàng nào.</p>
          ) : (
            <p className="text-sm text-slate-500">
              Chọn mặt hàng từ kho {selectedWarehouse?.name ?? "đã chọn"} và thêm vào danh sách.
            </p>
          )}
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex items-center justify-between gap-3">
          <h3 className="text-sm font-semibold text-slate-900">Danh sách cấp phát</h3>
          <span className="text-xs text-slate-500">{selectedItems.length} mặt hàng</span>
        </div>

        {selectedItems.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
            Chưa có mặt hàng nào được thêm.
          </div>
        ) : (
          <div className="space-y-2">
            {selectedItems.map((item) => (
              <div
                key={item.reliefItemId}
                className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div>
                  <p className="font-semibold text-slate-900">{item.itemName}</p>
                  <p className="text-sm text-slate-500">
                    Phân phối {item.quantity} {item.unit} · tồn kho hiện tại {item.stock} {item.unit}
                  </p>
                </div>
                <Button type="button" variant="ghost" onClick={() => removeItem(item.reliefItemId)}>
                  <Trash2 className="h-4 w-4" />
                  Xóa
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="space-y-2">
        <label className="text-sm font-medium text-slate-700">Ghi chú</label>
        <textarea
          value={note}
          onChange={(event) => setNote(event.target.value)}
          rows={4}
          placeholder="Ghi chú về nội dung hoặc lý do phân phối"
          className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none ring-0 placeholder:text-slate-400 focus:border-teal-500"
        />
      </div>

      <div className="flex flex-wrap gap-3">
        <Button
          type="button"
          onClick={() => createMutation.mutate()}
          disabled={createMutation.isPending || selectedItems.length === 0}
          className="min-w-40 justify-center"
        >
          {createMutation.isPending ? "Đang lưu..." : "Ghi nhận phân phối"}
        </Button>
      </div>
    </Card>
  );
}