"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";

import { register as registerApi } from "@/features/auth/services/auth.service";

const PASSWORD_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
const PHONE_REGEX = /^(0|\+84)[0-9]{9,10}$/;

const registerSchema = z
  .object({
    fullName: z.string().min(2, "Ho ten phai co it nhat 2 ky tu").max(100, "Ho ten toi da 100 ky tu"),
    phone: z
      .string()
      .regex(PHONE_REGEX, "So dien thoai khong hop le (phai bat dau bang 0 hoac +84)"),
    email: z.string().email("Email khong hop le").optional().or(z.literal("")),
    password: z
      .string()
      .regex(
        PASSWORD_REGEX,
        "Mat khau toi thieu 8 ky tu, gom chu hoa, chu thuong, so va ky tu dac biet (@$!%*?&)",
      ),
    confirmPassword: z.string(),
    role: z.enum(["CITIZEN", "RESCUE_TEAM"]),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Mat khau xac nhan khong khop",
    path: ["confirmPassword"],
  });

type RegisterFormInput = z.infer<typeof registerSchema>;

export function RegisterForm() {
  const router = useRouter();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormInput>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      fullName: "",
      phone: "",
      email: "",
      password: "",
      confirmPassword: "",
      role: "CITIZEN",
    },
  });

  const onSubmit = async (payload: RegisterFormInput) => {
    try {
      await registerApi({
        fullName: payload.fullName,
        phone: payload.phone,
        email: payload.email || undefined,
        password: payload.password,
        role: payload.role,
      });
      toast.success("Dang ky thanh cong, vui long dang nhap");
      router.replace("/login");
    } catch (error: unknown) {
      // Extract backend validation message if available
      const axiosErr = error as { response?: { data?: { message?: string } }; message?: string };
      const msg =
        axiosErr?.response?.data?.message ||
        (error instanceof Error ? error.message : null) ||
        "Dang ky that bai";
      toast.error(msg);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Ho va ten</label>
        <input
          {...register("fullName")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
          placeholder="Nguyen Van A"
        />
        {errors.fullName ? (
          <p className="mt-1 text-sm text-red-600">{errors.fullName.message}</p>
        ) : null}
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">So dien thoai</label>
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
        <label className="mb-1 block text-sm font-medium text-slate-700">Email (tuy chon)</label>
        <input
          {...register("email")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
          placeholder="user@example.com"
        />
        {errors.email ? (
          <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
        ) : null}
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Vai tro</label>
        <select
          {...register("role")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
        >
          <option value="CITIZEN">CITIZEN</option>
          <option value="RESCUE_TEAM">RESCUE_TEAM</option>
        </select>
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Mat khau</label>
        <input
          type="password"
          {...register("password")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
        />
        {errors.password ? (
          <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
        ) : null}
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-slate-700">Xac nhan mat khau</label>
        <input
          type="password"
          {...register("confirmPassword")}
          className="w-full rounded-xl border border-slate-300 px-4 py-2 outline-none focus:border-teal-600"
        />
        {errors.confirmPassword ? (
          <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
        ) : null}
      </div>

      <button
        disabled={isSubmitting}
        className="w-full rounded-xl bg-teal-700 px-4 py-2 font-semibold text-white transition hover:bg-teal-800 disabled:opacity-60"
        type="submit"
      >
        {isSubmitting ? "Dang xu ly..." : "Dang ky"}
      </button>

      <p className="text-sm text-slate-600">
        Da co tai khoan?{" "}
        <Link className="font-semibold text-teal-700" href="/login">
          Dang nhap
        </Link>
      </p>
    </form>
  );
}
