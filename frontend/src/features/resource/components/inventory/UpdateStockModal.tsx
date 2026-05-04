"use client";

import * as React from "react";
import { Modal } from "@/components/ui/modal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { updateStock } from "@/features/resource/services/resource.service";
import { type ReliefItem } from "@/features/resource/types/resource.types";
import { toast } from "sonner";
import { ArrowUpRight, ArrowDownRight } from "lucide-react";

interface UpdateStockModalProps {
  item: ReliefItem;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function UpdateStockModal({ item, open, onClose, onSuccess }: UpdateStockModalProps) {
  const [adjustment, setAdjustment] = React.useState<number>(0);
  const [note, setNote] = React.useState("");
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (adjustment === 0) return;

    setIsSubmitting(true);
    try {
      await updateStock(item.id, { quantity: adjustment, note });
      toast.success(`Đã ${adjustment > 0 ? 'nhập' : 'xuất'} ${Math.abs(adjustment)} ${item.unit} ${item.name}`);
      onSuccess();
    } catch (error: unknown) {
      console.error("Update stock error:", error);
      const message = error instanceof Error ? error.message : "Không thể cập nhật tồn kho";
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const finalQuantity = item.quantity + adjustment;

  return (
    <Modal open={open} onClose={onClose} title={`Cập nhật tồn kho: ${item.name}`}>
      <form onSubmit={handleSubmit} className="space-y-4 pt-2">
        <div className="p-4 rounded-2xl bg-slate-50 border border-slate-100 flex justify-between items-center">
          <div>
            <p className="text-[10px] text-slate-400 uppercase font-black tracking-widest">Hiện tại</p>
            <p className="text-xl font-black text-slate-900">{item.quantity} <span className="text-xs text-slate-400 uppercase">{item.unit}</span></p>
          </div>
          <div className="text-right">
            <p className="text-[10px] text-slate-400 uppercase font-black tracking-widest">Dự kiến</p>
            <p className={`text-xl font-black ${finalQuantity < 0 ? 'text-red-600' : 'text-brand-600'}`}>
              {finalQuantity} <span className="text-xs opacity-50 uppercase">{item.unit}</span>
            </p>
          </div>
        </div>

        <div className="space-y-2">
          <label className="block text-sm font-bold text-slate-700">
            Số lượng thay đổi (Số âm để xuất kho)
          </label>
          <div className="relative">
            <Input 
              type="number"
              value={adjustment}
              onChange={(e) => setAdjustment(parseInt(e.target.value) || 0)}
              className="pl-10 font-bold text-lg h-12"
            />
            {adjustment >= 0 ? (
              <ArrowUpRight className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-emerald-500" />
            ) : (
              <ArrowDownRight className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-red-500" />
            )}
          </div>
          <div className="flex gap-2 flex-wrap pt-1">
            {[10, 50, 100, -10, -50, -100].map(val => (
              <button
                key={val}
                type="button"
                onClick={() => setAdjustment(prev => prev + val)}
                className="text-[10px] px-3 py-1.5 rounded-full bg-white border border-slate-200 hover:border-brand-500 hover:text-brand-600 text-slate-600 font-black transition-all"
              >
                {val > 0 ? `+${val}` : val}
              </button>
            ))}
          </div>
        </div>

        <div className="space-y-2">
          <label className="block text-sm font-bold text-slate-700">
            Ghi chú giao dịch
          </label>
          <Input 
            placeholder="VD: Nhập hàng từ xe cứu trợ #42..."
            value={note}
            onChange={(e) => setNote(e.target.value)}
            className="text-sm"
          />
        </div>

        <div className="flex justify-end gap-3 pt-6">
          <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting} className="font-bold">
            Bỏ qua
          </Button>
          <Button 
            type="submit" 
            variant={adjustment < 0 ? "danger" : "default"}
            disabled={isSubmitting || adjustment === 0 || finalQuantity < 0}
            className="font-black px-6"
          >
            {isSubmitting ? "Đang xử lý..." : adjustment > 0 ? "Xác nhận nhập kho" : "Xác nhận xuất kho"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
