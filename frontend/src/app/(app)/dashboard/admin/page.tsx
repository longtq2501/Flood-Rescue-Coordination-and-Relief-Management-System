import Link from "next/link";
import { Shield, Users, Activity, LayoutDashboard, ArrowUpRight, Settings2, CircleSlash } from "lucide-react";

const quickLinks = [
  { href: "/dashboard/manager", label: "Quản lý", description: "Theo dõi kho, phương tiện và phân phối", icon: LayoutDashboard },
  { href: "/dashboard/coordinator", label: "Điều phối", description: "Xác minh yêu cầu và phân công nhiệm vụ", icon: Activity },
  { href: "/dashboard/citizen", label: "Người dân", description: "Xem luồng tạo yêu cầu và theo dõi", icon: Users },
  { href: "/dashboard/rescue-team", label: "Đội cứu hộ", description: "Theo dõi nhiệm vụ và vị trí đội", icon: Shield },
];

const governanceItems = [
  "Giám sát toàn cục trạng thái hệ thống và phân quyền",
  "Kiểm tra nhanh dashboard theo từng vai trò để đối chiếu luồng nghiệp vụ",
  "Xem trạng thái hoạt động của các module chính trước khi demo",
  "Chuẩn bị nền tảng cho màn hình quản trị người dùng và cấu hình sau này",
];

export default function AdminDashboardPage() {
  return (
    <div className="space-y-6">
      <section className="overflow-hidden rounded-3xl border border-slate-200 bg-gradient-to-r from-slate-900 via-slate-800 to-cyan-900 px-5 py-6 text-white shadow-sm sm:px-6">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div className="max-w-3xl space-y-3">
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-cyan-200">ADMIN DASHBOARD</p>
            <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">Điều hành hệ thống Flood Rescue</h1>
            <p className="max-w-2xl text-sm leading-6 text-slate-200/90 sm:text-base">
              Khu vực tổng quan cho quản trị viên: xem nhanh trạng thái các vai trò, chuẩn bị kiểm tra luồng demo,
              và làm nền cho màn hình quản trị người dùng sau này.
            </p>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/10 px-4 py-3 backdrop-blur-sm">
            <div className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-cyan-400/20 text-cyan-100">
                <Settings2 className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-cyan-100/80">Phạm vi</p>
                <p className="font-semibold">System-wide oversight</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-100 text-slate-700">
              <Shield className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-slate-500">Quyền quản trị</p>
              <p className="text-xl font-bold text-slate-900">ADMIN</p>
            </div>
          </div>
          <p className="mt-4 text-sm leading-6 text-slate-600">
            Role này không cho register tự do, nên cần account seed sẵn để demo và kiểm tra toàn hệ thống.
          </p>
        </article>

        <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-cyan-50 text-cyan-700">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-slate-500">Vai trò đang có UI</p>
              <p className="text-xl font-bold text-slate-900">5 / 5</p>
            </div>
          </div>
          <p className="mt-4 text-sm leading-6 text-slate-600">
            Citizen, Rescue Team, Coordinator, Manager và Admin đều đã có route riêng trong dashboard.
          </p>
        </article>

        <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-700">
              <Activity className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm text-slate-500">Mục tiêu demo</p>
              <p className="text-xl font-bold text-slate-900">Kiểm tra luồng</p>
            </div>
          </div>
          <p className="mt-4 text-sm leading-6 text-slate-600">
            Dùng để kiểm tra nhanh điều phối, tồn kho, phân phối cứu trợ và trạng thái các role.
          </p>
        </article>
      </section>

      <section className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold text-slate-900">Lối tắt theo vai trò</h2>
              <p className="text-sm text-slate-500">Đi nhanh đến các dashboard chính để kiểm tra flow demo.</p>
            </div>
          </div>

          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            {quickLinks.map((item) => {
              const Icon = item.icon;

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className="group rounded-2xl border border-slate-200 bg-slate-50 p-4 transition hover:-translate-y-0.5 hover:border-cyan-200 hover:bg-cyan-50/60"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3">
                      <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-cyan-700 shadow-sm ring-1 ring-slate-200 group-hover:ring-cyan-100">
                        <Icon className="h-5 w-5" />
                      </div>
                      <div>
                        <p className="font-semibold text-slate-900">{item.label}</p>
                        <p className="mt-1 text-sm leading-6 text-slate-500">{item.description}</p>
                      </div>
                    </div>
                    <ArrowUpRight className="mt-1 h-4 w-4 text-slate-400 transition group-hover:translate-x-0.5 group-hover:-translate-y-0.5 group-hover:text-cyan-700" />
                  </div>
                </Link>
              );
            })}
          </div>
        </div>

        <aside className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900">Phạm vi quản trị</h2>
          <p className="mt-1 text-sm text-slate-500">Danh sách ngắn các nhiệm vụ mà admin cần nắm.</p>

          <ul className="mt-4 space-y-3">
            {governanceItems.map((item) => (
              <li key={item} className="flex gap-3 rounded-2xl bg-slate-50 px-4 py-3 text-sm leading-6 text-slate-700">
                <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-cyan-600" />
                <span>{item}</span>
              </li>
            ))}
          </ul>

          <div className="mt-5 rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-500">
            UI admin hiện mới ở mức tối thiểu để đủ route và đủ demo. Khi cần, có thể mở rộng thành quản lý user,
            cấu hình hệ thống, và audit log.
          </div>
        </aside>
      </section>
    </div>
  );
}