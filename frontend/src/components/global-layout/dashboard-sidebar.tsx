"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Box, Home, LayoutDashboard, LogOut, MapPinned, Package, Send, ShieldCheck, Truck, Users } from "lucide-react";
import clsx from "clsx";

import { useAuthStore } from "@/features/auth/store/auth.store";
import type { AppRole } from "@/shared/constants/auth";

type SidebarItem = {
  href: string;
  label: string;
  icon: typeof Home;
};

type SidebarGroup = {
  title: string;
  items: SidebarItem[];
};

const SIDEBAR_CONFIG: Record<AppRole, { title: string; subtitle: string; groups: SidebarGroup[] }> = {
  CITIZEN: {
    title: "CITIZEN",
    subtitle: "Khu vực tiếp nhận và theo dõi yêu cầu cứu hộ",
    groups: [
      {
        title: "Thao tác chính",
        items: [
          { href: "/dashboard/citizen#create-request", label: "Tạo yêu cầu", icon: Home },
          { href: "/dashboard/citizen#my-requests", label: "Yêu cầu của tôi", icon: LayoutDashboard },
        ],
      },
      {
        title: "Tài khoản",
        items: [{ href: "/settings/profile", label: "Hồ sơ cá nhân", icon: Users }],
      },
    ],
  },
  COORDINATOR: {
    title: "COORDINATOR",
    subtitle: "Điều phối, xác minh và phân công cứu hộ",
    groups: [
      {
        title: "Điều hành",
        items: [
          { href: "/dashboard/coordinator", label: "Bảng điều phối", icon: LayoutDashboard },
          { href: "/dashboard/coordinator/map", label: "Bản đồ cứu trợ", icon: MapPinned },
        ],
      },
      {
        title: "Tài khoản",
        items: [{ href: "/settings/profile", label: "Hồ sơ cá nhân", icon: Users }],
      },
    ],
  },
  RESCUE_TEAM: {
    title: "RESCUE_TEAM",
    subtitle: "Nhận và xử lý nhiệm vụ cứu hộ",
    groups: [
      {
        title: "Nhiệm vụ",
        items: [{ href: "/dashboard/rescue-team", label: "Nhiệm vụ của tôi", icon: ShieldCheck }],
      },
      {
        title: "Tài khoản",
        items: [{ href: "/settings/profile", label: "Hồ sơ cá nhân", icon: Users }],
      },
    ],
  },
  MANAGER: {
    title: "MANAGER",
    subtitle: "Theo dõi tổng quan, kho hàng và nguồn lực",
    groups: [
      {
        title: "Quản trị",
        items: [
          { href: "/dashboard/manager", label: "Tổng quan", icon: LayoutDashboard },
          { href: "/dashboard/manager/inventory", label: "Tồn kho", icon: Package },
          { href: "/dashboard/manager/distributions", label: "Phân phối cứu trợ", icon: Send },
          { href: "/dashboard/manager/warehouses", label: "Kho hàng", icon: Box },
          { href: "/dashboard/manager/teams", label: "Đội cứu hộ", icon: ShieldCheck },
          { href: "/dashboard/manager/vehicles", label: "Phương tiện", icon: Truck },
        ],
      },
      {
        title: "Tài khoản",
        items: [{ href: "/settings/profile", label: "Hồ sơ cá nhân", icon: Users }],
      },
    ],
  },
  ADMIN: {
    title: "ADMIN",
    subtitle: "Quản lý hệ thống và giám sát toàn cục",
    groups: [
      {
        title: "Hệ thống",
        items: [{ href: "/dashboard/admin", label: "Tổng quan hệ thống", icon: LayoutDashboard }],
      },
      {
        title: "Tài khoản",
        items: [{ href: "/settings/profile", label: "Hồ sơ cá nhân", icon: Users }],
      },
    ],
  },
};

function isActive(pathname: string, href: string) {
  const baseHref = href.split("#")[0];
  return pathname === baseHref || pathname.startsWith(`${baseHref}/`);
}

export function DashboardSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const role = useAuthStore((state) => state.role);
  const hydrated = useAuthStore((state) => state.hydrated);
  const clearSession = useAuthStore((state) => state.clearSession);

  if (!hydrated || !role) {
    return (
      <aside className="fixed inset-y-0 left-0 z-10 w-72 border-r border-slate-200 bg-white/95 p-4 shadow-sm backdrop-blur md:static md:translate-x-0">
        <div className="space-y-4 animate-pulse">
          <div className="h-20 rounded-2xl bg-slate-100" />
          <div className="h-10 rounded-xl bg-slate-100" />
          <div className="h-10 rounded-xl bg-slate-100" />
          <div className="h-10 rounded-xl bg-slate-100" />
        </div>
      </aside>
    );
  }

  const config = SIDEBAR_CONFIG[role] ?? SIDEBAR_CONFIG.CITIZEN;

  const handleLogout = () => {
    clearSession();
    router.push("/");
  };

  return (
    <aside className="fixed inset-y-0 left-0 z-10 w-72 overflow-y-auto border-r border-slate-200 bg-white/95 shadow-sm backdrop-blur md:static md:translate-x-0">
      <div className="border-b border-slate-100 px-5 py-5">
        <p className="text-xs font-semibold uppercase tracking-[0.24em] text-teal-600">Flood Rescue</p>
        <h2 className="mt-2 text-lg font-bold text-slate-900">{config.title}</h2>
        <p className="mt-1 text-sm leading-6 text-slate-500">{config.subtitle}</p>
      </div>

      <nav className="space-y-6 px-4 py-5">
        {config.groups.map((group) => (
          <section key={group.title} className="space-y-2">
            <h3 className="px-2 text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">
              {group.title}
            </h3>
            <div className="space-y-1">
              {group.items.map((item) => {
                const active = isActive(pathname, item.href);
                const Icon = item.icon;

                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={clsx(
                      "flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium transition-colors",
                      active
                        ? "bg-teal-50 text-teal-800 ring-1 ring-teal-100"
                        : "text-slate-700 hover:bg-slate-50 hover:text-slate-900",
                    )}
                  >
                    <span
                      className={clsx(
                        "flex h-9 w-9 items-center justify-center rounded-xl",
                        active ? "bg-teal-600 text-white" : "bg-slate-100 text-slate-500",
                      )}
                    >
                      <Icon className="h-4 w-4" />
                    </span>
                    <span className="flex-1">{item.label}</span>
                  </Link>
                );
              })}
            </div>
          </section>
        ))}

        <div className="border-t border-slate-100 pt-4">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-100 text-slate-500">
              <LogOut className="h-4 w-4" />
            </span>
            <span className="flex-1 text-left">Đăng xuất</span>
          </button>
        </div>
      </nav>
    </aside>
  );
}