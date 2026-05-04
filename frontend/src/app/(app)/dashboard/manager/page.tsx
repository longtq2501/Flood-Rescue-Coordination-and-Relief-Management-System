import { AnalyticsDashboard } from "@/features/report/components/analytics-dashboard";

export default function ManagerDashboardPage() {
  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-900">Bảng điều khiển quản lý</h1>
        <p className="text-slate-500">Phân tích và giám sát hoạt động cứu trợ thời gian thực</p>
      </div>
      <AnalyticsDashboard />
    </div>
  );
}
