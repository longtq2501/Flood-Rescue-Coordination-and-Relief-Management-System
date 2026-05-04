"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { toast } from "sonner";
import { useMutation } from "@tanstack/react-query";

import { Button, Input } from "@/components/ui";
import { changePassword } from "../services/auth.service";
import type { ChangePasswordRequest } from "@/shared/types/api";

const passwordSchema = z.object({
  oldPassword: z.string().min(6, "Mật khẩu cũ phải ít nhất 6 ký tự"),
  newPassword: z.string().min(6, "Mật khẩu mới phải ít nhất 6 ký tự"),
  confirmPassword: z.string(),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"],
});

type PasswordFormValues = z.infer<typeof passwordSchema>;

export function ChangePasswordForm() {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PasswordFormValues>({
    resolver: zodResolver(passwordSchema),
  });

  const mutation = useMutation({
    mutationFn: (data: ChangePasswordRequest) => changePassword(data),
    onSuccess: () => {
      toast.success("Đổi mật khẩu thành công");
      reset();
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Đổi mật khẩu thất bại");
    },
  });

  const onSubmit = (data: PasswordFormValues) => {
    mutation.mutate({
      oldPassword: data.oldPassword,
      newPassword: data.newPassword,
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-md">
      <div className="space-y-2">
        <label className="text-sm font-medium text-slate-700">Mật khẩu hiện tại</label>
        <Input 
          type="password" 
          {...register("oldPassword")} 
          placeholder="••••••••" 
        />
        {errors.oldPassword && (
          <p className="text-xs text-red-500">{errors.oldPassword.message}</p>
        )}
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium text-slate-700">Mật khẩu mới</label>
        <Input 
          type="password" 
          {...register("newPassword")} 
          placeholder="••••••••" 
        />
        {errors.newPassword && (
          <p className="text-xs text-red-500">{errors.newPassword.message}</p>
        )}
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium text-slate-700">Xác nhận mật khẩu mới</label>
        <Input 
          type="password" 
          {...register("confirmPassword")} 
          placeholder="••••••••" 
        />
        {errors.confirmPassword && (
          <p className="text-xs text-red-500">{errors.confirmPassword.message}</p>
        )}
      </div>
      <div className="flex justify-end">
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? "Đang xử lý..." : "Cập nhật mật khẩu"}
        </Button>
      </div>
    </form>
  );
}
