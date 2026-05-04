"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";

import { login } from "@/features/auth/services/auth.service";
import { useAuthStore } from "@/features/auth/store/auth.store";
import { ROLE_TO_DASHBOARD_PATH } from "@/shared/constants/auth";

const loginSchema = z.object({
  phone: z
    .string()
    .min(10, "So dien thoai khong hop le")
    .max(11, "So dien thoai khong hop le"),
  password: z.string().min(8, "Mat khau toi thieu 8 ky tu"),
});

type LoginFormInput = z.infer<typeof loginSchema>;

export function LoginForm() {
  const router = useRouter();
  const setSession = useAuthStore((state) => state.setSession);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormInput>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      phone: "",
      password: "",
    },
  });

  const onSubmit = async (payload: LoginFormInput) => {
    try {
      const result = await login(payload);
      setSession(result.user);
      toast.success("Dang nhap thanh cong");
      router.replace(ROLE_TO_DASHBOARD_PATH[result.user.role]);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : "Dang nhap that bai");
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">
          So dien thoai
        </label>
        <input
          {...register("phone")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
          placeholder="0901234567"
        />
        {errors.phone ? (
          <p className="mt-1 text-sm text-red-600">{errors.phone.message}</p>
        ) : null}
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Mat khau</label>
        <input
          type="password"
          {...register("password")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
          placeholder="Password@123"
        />
        {errors.password ? (
          <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
        ) : null}
      </div>

      <button
        disabled={isSubmitting}
        className="w-full rounded-xl bg-teal-700 px-4 py-2 font-semibold text-white transition hover:bg-teal-800 disabled:opacity-60"
        type="submit"
      >
        {isSubmitting ? "Dang xu ly..." : "Dang nhap"}
      </button>

      <p className="text-sm text-slate-600">
        Chua co tai khoan?{" "}
        <Link className="font-semibold text-teal-700" href="/register">
          Dang ky
        </Link>
      </p>
    </form>
  );
}
