"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { addVehicle } from "../../services/resource.service";
import { useState } from "react";
import { toast } from "sonner";

const vehicleSchema = z.object({
  plateNumber: z.string().min(1, "Biển số không được để trống"),
  type: z.enum(["BOAT", "TRUCK", "HELICOPTER", "AMBULANCE", "OTHER"]),
  capacity: z.coerce.number().min(1, "Sức chứa phải ít nhất là 1"),
});

type VehicleFormData = z.infer<typeof vehicleSchema>;

interface AddVehicleFormProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export function AddVehicleForm({ onSuccess, onCancel }: AddVehicleFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<VehicleFormData>({
    resolver: zodResolver(vehicleSchema),
    defaultValues: {
      type: "BOAT",
      capacity: 1,
    },
  });

  const onSubmit = async (data: VehicleFormData) => {
    setIsSubmitting(true);
    try {
      await addVehicle(data);
      toast.success("Thêm phương tiện thành công");
      onSuccess();
    } catch (error: any) {
      toast.error(error.message || "Không thể thêm phương tiện");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">
          Biển số / Định danh
        </label>
        <Input
          {...register("plateNumber")}
          placeholder="VD: 51A-12345"
          className={errors.plateNumber ? "border-red-500" : ""}
        />
        {errors.plateNumber && (
          <p className="mt-1 text-xs text-red-500">{errors.plateNumber.message}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">
          Loại phương tiện
        </label>
        <select
          {...register("type")}
          className="w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        >
          <option value="BOAT">Cano / Thuyền</option>
          <option value="TRUCK">Xe tải / Xe lội nước</option>
          <option value="HELICOPTER">Trực thăng</option>
          <option value="AMBULANCE">Xe cấp cứu</option>
          <option value="OTHER">Khác</option>
        </select>
        {errors.type && (
          <p className="mt-1 text-xs text-red-500">{errors.type.message}</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-slate-700 mb-1">
          Sức chứa (số người)
        </label>
        <Input
          type="number"
          {...register("capacity")}
          className={errors.capacity ? "border-red-500" : ""}
        />
        {errors.capacity && (
          <p className="mt-1 text-xs text-red-500">{errors.capacity.message}</p>
        )}
      </div>

      <div className="flex justify-end gap-3 pt-4">
        <Button
          type="button"
          variant="ghost"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Hủy
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Đang lưu..." : "Lưu phương tiện"}
        </Button>
      </div>
    </form>
  );
}
