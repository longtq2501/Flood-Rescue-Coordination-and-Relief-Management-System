"use client";

import * as React from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Modal } from "@/components/ui/modal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { createTeam, updateTeam } from "@/features/dispatch/services/dispatch.service";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

const teamSchema = z.object({
  name: z.string().min(1, "Tên đội không được để trống"),
  leaderId: z.coerce.number().min(1, "Vui lòng nhập ID trưởng đội hợp lệ"),
  capacity: z.coerce.number().min(1, "Sức chứa tối thiểu là 1").max(20, "Sức chứa tối đa là 20"),
});

type TeamFormValues = z.infer<typeof teamSchema>;

interface AddTeamModalProps {
  open: boolean;
  onClose: () => void;
  team?: any;
}

export function AddTeamModal({ open, onClose, team }: AddTeamModalProps) {
  const queryClient = useQueryClient();
  const isEdit = !!team;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<TeamFormValues>({
    resolver: zodResolver(teamSchema),
    defaultValues: {
      name: "",
      leaderId: 1,
      capacity: 4,
    },
  });

  React.useEffect(() => {
    if (team) {
      reset({
        name: team.name,
        leaderId: team.leaderId,
        capacity: team.capacity,
      });
    } else {
      reset({
        name: "",
        leaderId: 1,
        capacity: 4,
      });
    }
  }, [team, reset]);

  const mutation = useMutation({
    mutationFn: (data: TeamFormValues) => 
      isEdit ? updateTeam(team.id, data) : createTeam(data),
    onSuccess: () => {
      toast.success(isEdit ? "Đã cập nhật thông tin đội" : "Đã tạo đội cứu hộ mới");
      queryClient.invalidateQueries({ queryKey: ["teams"] });
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.message || "Có lỗi xảy ra");
    },
  });

  const onSubmit = (data: TeamFormValues) => {
    mutation.mutate(data);
  };

  return (
    <Modal 
      open={open} 
      onClose={onClose} 
      title={isEdit ? `Chỉnh sửa: ${team.name}` : "Tạo Đội cứu hộ mới"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-2">
        <div className="space-y-2">
          <label className="text-sm font-bold text-slate-700">Tên đội cứu hộ</label>
          <Input 
            {...register("name")} 
            placeholder="VD: Đội Phản ứng nhanh #1" 
            className={errors.name ? "border-red-500" : ""}
          />
          {errors.name && <p className="text-xs text-red-500">{errors.name.message}</p>}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700">ID Trưởng đội (User ID)</label>
            <Input 
              type="number"
              {...register("leaderId")} 
              className={errors.leaderId ? "border-red-500" : ""}
            />
            {errors.leaderId && <p className="text-xs text-red-500">{errors.leaderId.message}</p>}
          </div>
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700">Sức chứa (người)</label>
            <Input 
              type="number"
              {...register("capacity")} 
              className={errors.capacity ? "border-red-500" : ""}
            />
            {errors.capacity && <p className="text-xs text-red-500">{errors.capacity.message}</p>}
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-6">
          <Button type="button" variant="ghost" onClick={onClose} className="font-bold">
            Hủy
          </Button>
          <Button 
            type="submit" 
            disabled={mutation.isPending}
            className="bg-cyan-600 hover:bg-cyan-700 text-white font-black px-8"
          >
            {mutation.isPending ? "Đang lưu..." : isEdit ? "Lưu thay đổi" : "Tạo đội"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
