"use client";

import { useQuery } from "@tanstack/react-query";
import { User, Shield, Calendar, Mail, Phone, MapPin } from "lucide-react";

import { Card } from "@/components/ui";
import { getMe } from "@/features/auth/services/auth.service";
import { ProfileForm } from "@/features/auth/components/profile-form";
import { ChangePasswordForm } from "@/features/auth/components/change-password-form";

export default function ProfilePage() {
  const { data: user, isLoading, error } = useQuery({
    queryKey: ["auth-me"],
    queryFn: getMe,
  });

  if (isLoading) {
    return (
      <div className="flex h-[50vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-teal-600 border-t-transparent"></div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">
        <h3 className="text-lg font-bold">Lỗi tải thông tin</h3>
        <p>Không thể tải thông tin người dùng. Vui lòng thử lại sau.</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-4xl mx-auto pb-20">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">Hồ sơ cá nhân</h1>
        <p className="text-slate-500">Quản lý thông tin tài khoản và bảo mật của bạn</p>
      </div>

      <div className="grid gap-8 md:grid-cols-3">
        {/* Account Summary Sidebar */}
        <div className="md:col-span-1 space-y-6">
          <Card className="p-6">
            <div className="flex flex-col items-center text-center">
              <div className="h-24 w-24 rounded-full bg-teal-100 flex items-center justify-center mb-4 border-4 border-white shadow-sm">
                <User className="h-12 w-12 text-teal-600" />
              </div>
              <h2 className="text-xl font-bold text-slate-900">{user.fullName}</h2>
              <div className="mt-1 flex items-center gap-1.5 text-sm font-medium text-teal-600 bg-teal-50 px-2.5 py-0.5 rounded-full">
                <Shield size={14} />
                {user.role}
              </div>
            </div>

            <div className="mt-8 space-y-4">
              <div className="flex items-center gap-3 text-sm text-slate-600">
                <Mail size={16} className="text-slate-400" />
                <span className="truncate">{user.email || "Chưa cập nhật email"}</span>
              </div>
              <div className="flex items-center gap-3 text-sm text-slate-600">
                <Phone size={16} className="text-slate-400" />
                <span>{user.phone}</span>
              </div>
              <div className="flex items-center gap-3 text-sm text-slate-600">
                <MapPin size={16} className="text-slate-400" />
                <span>{user.address || "Chưa cập nhật địa chỉ"}</span>
              </div>
              <div className="flex items-center gap-3 text-sm text-slate-600">
                <Calendar size={16} className="text-slate-400" />
                <span>
                  Tham gia: {user.createdAt ? new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", timeZone: "UTC" }).format(new Date(user.createdAt)) : "N/A"}
                </span>
              </div>
            </div>
          </Card>
          
          <Card className="p-6">
            <h3 className="font-bold text-slate-900 mb-3">Quyền hạn hoạt động</h3>
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-1 rounded">
                <div className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                Truy cập Dashboard
              </div>
              <div className="flex items-center gap-2 text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-1 rounded">
                <div className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                Gửi yêu cầu cứu trợ
              </div>
              {user.role !== "CITIZEN" && (
                <div className="flex items-center gap-2 text-xs font-medium text-emerald-700 bg-emerald-50 px-2 py-1 rounded">
                  <div className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                  Quản lý hệ thống
                </div>
              )}
            </div>
          </Card>
        </div>

        {/* Main Forms Section */}
        <div className="md:col-span-2 space-y-6">
          <Card className="p-6">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
              <User size={20} className="text-teal-600" />
              Thông tin cơ bản
            </h3>
            <ProfileForm user={user} />
          </Card>

          <Card className="p-6">
            <h3 className="text-lg font-bold text-slate-900 mb-6 flex items-center gap-2">
              <Shield size={20} className="text-teal-600" />
              Bảo mật tài khoản
            </h3>
            <ChangePasswordForm />
          </Card>
        </div>
      </div>
    </div>
  );
}
