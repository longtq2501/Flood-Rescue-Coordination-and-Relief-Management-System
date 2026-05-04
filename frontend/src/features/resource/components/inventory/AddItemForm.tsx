"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { addItem } from "@/features/resource/services/resource.service";
import { useState } from "react";
import { toast } from "sonner";

const itemSchema = z.object({
  name: z.string().min(1, "Tên hàng hóa không được để trống"),
  category: z.string().min(1, "Danh mục không được để trống"),
  unit: z.string().min(1, "Đơn vị không được để trống"),
  quantity: z.coerce.number().min(0, "Số lượng không được nhỏ hơn 0"),
  lowThreshold: z.coerce.number().min(1, "Ngưỡng báo động phải ít nhất là 1"),
});

type ItemFormData = z.infer<typeof itemSchema>;

interface AddItemFormProps {
  warehouseId: number;
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddItemForm({ warehouseId, onSuccess, onCancel }: AddItemFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ItemFormData>({
    resolver: zodResolver(itemSchema),
    defaultValues: {
      category: "Nhu yếu phẩm",
      unit: "Thùng",
      quantity: 0,
      lowThreshold: 10,
    },
  });

  const onSubmit = async (data: ItemFormData) => {
    setIsSubmitting(true);
    try {
      await addItem({ ...data, warehouseId });
      toast.success("Thêm hàng hóa vào kho thành công");
      onSuccess();
    } catch (error: any) {
      toast.error(error.message || "Không thể thêm hàng hóa");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-2">
      <div className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <label className="block text-sm font-medium text-slate-700 mb-1">Tên hàng hóa</label>
          <Input {...register("name")} placeholder="VD: Mì tôm Hảo Hảo" />
          {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
        </div>
        
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Danh mục</label>
          <Input {...register("category")} placeholder="VD: Thực phẩm" />
          {errors.category && <p className="mt-1 text-xs text-red-500">{errors.category.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Đơn vị tính</label>
          <Input {...register("unit")} placeholder="VD: Thùng, Chai, Gói..." />
          {errors.unit && <p className="mt-1 text-xs text-red-500">{errors.unit.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Số lượng ban đầu</label>
          <Input type="number" {...register("quantity")} />
          {errors.quantity && <p className="mt-1 text-xs text-red-500">{errors.quantity.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Ngưỡng báo động</label>
          <Input type="number" {...register("lowThreshold")} />
          {errors.lowThreshold && <p className="mt-1 text-xs text-red-500">{errors.lowThreshold.message}</p>}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-4">
        <Button type="button" variant="ghost" onClick={onCancel} disabled={isSubmitting}>
          Hủy
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Đang lưu..." : "Thêm vào kho"}
        </Button>
      </div>
    </form>
  );
}
