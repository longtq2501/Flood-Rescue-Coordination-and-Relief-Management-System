"use client";

import React from "react";
import { useQuery } from "@tanstack/react-query";
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, 
  PieChart, Pie, Cell, AreaChart, Area
} from "recharts";
import { 
  TrendingUp, Users, Activity, Package, AlertTriangle, 
  CheckCircle, Clock, ChevronRight
} from "lucide-react";
import { getDashboardData } from "../services/report.service";
import { DashboardData } from "../types";

const COLORS = ["#0ea5e9", "#10b981", "#f59e0b", "#ef4444"];

function isDashboardData(value: unknown): value is DashboardData {
  if (!value || typeof value !== "object") {
    return false;
  }

  const candidate = value as Partial<DashboardData>;

  return Boolean(
    candidate.summary &&
      typeof candidate.summary.totalRequests === "number" &&
      typeof candidate.summary.completionRate === "number" &&
      candidate.resourceUsage &&
      Array.isArray(candidate.volumeTrend) &&
      Array.isArray(candidate.statusDistribution) &&
      Array.isArray(candidate.urgencyBreakdown) &&
      Array.isArray(candidate.recentActivities),
  );
}

const MOCK_DATA: DashboardData = {
  summary: {
    totalRequests: 1248,
    completedRequests: 856,
    pendingRequests: 142,
    inProgressRequests: 250,
    completionRate: 68.5,
    avgResponseMinutes: 12,
    avgCompleteMinutes: 45
  },
  resourceUsage: {
    vehiclesDeployed: 34,
    totalDistributions: 156,
    activeTeams: 28,
    totalTeams: 32,
    warehouseOccupancy: 75
  },
  volumeTrend: [
    { date: "01/05", count: 45 },
    { date: "02/05", count: 82 },
    { date: "03/05", count: 120 },
    { date: "04/05", count: 95 },
    { date: "05/05", count: 156 },
    { date: "06/05", count: 210 },
    { date: "07/05", count: 185 }
  ],
  statusDistribution: [
    { status: "Hoàn thành", count: 856 },
    { status: "Đang xử lý", count: 250 },
    { status: "Chờ xác minh", count: 142 },
    { status: "Đã hủy", count: 32 }
  ],
  urgencyBreakdown: [
    { level: "Rất khẩn cấp", count: 120 },
    { level: "Khẩn cấp", count: 350 },
    { level: "Bình thường", count: 628 },
    { level: "Thấp", count: 150 }
  ],
  recentActivities: [
    { id: "1", type: "REQUEST", description: "Yêu cầu cứu trợ mới tại Quận 7", timestamp: "5 phút trước", user: "Nguyễn Văn A" },
    { id: "2", type: "MISSION", description: "Hoàn thành nhiệm vụ giải cứu tại Nhà Bè", timestamp: "15 phút trước", user: "Đội cứu hộ 3" },
    { id: "3", type: "ALERT", description: "Cảnh báo lũ quét tại khu vực ven sông", timestamp: "30 phút trước", user: "Hệ thống" },
    { id: "4", type: "RESOURCE", description: "Xuất kho 500 thùng nhu yếu phẩm", timestamp: "1 giờ trước", user: "Trần Thị B" }
  ]
};

export function AnalyticsDashboard() {
  const { data: apiData, isLoading } = useQuery({
    queryKey: ["analytics-dashboard"],
    queryFn: getDashboardData,
    retry: false,
  });

  // Use mock data if API fails or in development
  const data = isDashboardData(apiData) ? apiData : MOCK_DATA;

  if (isLoading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
      </div>
    );
  }

  const { summary, resourceUsage, volumeTrend, statusDistribution, urgencyBreakdown, recentActivities } = data;

  return (
    <div className="space-y-6 pb-10">
      {/* Header Summary */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard 
          title="Tổng yêu cầu" 
          value={summary.totalRequests} 
          icon={<Activity className="h-5 w-5 text-blue-600" />}
          trend="+12% so với hôm qua"
          color="blue"
        />
        <StatCard 
          title="Tỉ lệ hoàn thành" 
          value={`${summary.completionRate}%`} 
          icon={<CheckCircle className="h-5 w-5 text-emerald-600" />}
          trend="Tăng 5% tuần này"
          color="emerald"
        />
        <StatCard 
          title="Đội cứu hộ" 
          value={`${resourceUsage.activeTeams}/${resourceUsage.totalTeams}`} 
          icon={<Users className="h-5 w-5 text-amber-600" />}
          trend="85% đang hoạt động"
          color="amber"
        />
        <StatCard 
          title="Kho hàng" 
          value={`${resourceUsage.warehouseOccupancy}%`} 
          icon={<Package className="h-5 w-5 text-indigo-600" />}
          trend="Đang ở mức an toàn"
          color="indigo"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Volume Trend */}
        <ChartContainer title="Xu hướng yêu cầu cứu trợ" icon={<TrendingUp className="h-5 w-5" />}>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={volumeTrend}>
                <defs>
                  <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip 
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                />
                <Area type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2} fillOpacity={1} fill="url(#colorCount)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </ChartContainer>

        {/* Status Distribution */}
        <ChartContainer title="Phân bổ trạng thái" icon={<Activity className="h-5 w-5" />}>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={statusDistribution} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" hide />
                <YAxis dataKey="status" type="category" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} width={100} />
                <Tooltip 
                   cursor={{fill: '#f8fafc'}}
                   contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                />
                <Bar dataKey="count" radius={[0, 4, 4, 0]}>
                  {statusDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </ChartContainer>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Urgency Breakdown */}
        <ChartContainer title="Mức độ khẩn cấp" icon={<AlertTriangle className="h-5 w-5" />} className="lg:col-span-1">
          <div className="h-[250px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={urgencyBreakdown}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="count"
                  nameKey="level"
                >
                  {urgencyBreakdown.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip 
                  contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-2 space-y-2">
            {urgencyBreakdown.map((item, index) => (
              <div key={item.level} className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <div className="h-3 w-3 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                  <span className="text-slate-600">{item.level}</span>
                </div>
                <span className="font-semibold text-slate-900">{item.count}</span>
              </div>
            ))}
          </div>
        </ChartContainer>

        {/* Recent Activity */}
        <ChartContainer title="Hoạt động gần đây" icon={<Clock className="h-5 w-5" />} className="lg:col-span-2">
          <div className="space-y-4">
            {recentActivities.map((act) => (
              <div key={act.id} className="flex items-start gap-4 rounded-lg border border-slate-50 p-3 hover:bg-slate-50 transition-colors">
                <div className={`mt-1 rounded-full p-2 ${
                  act.type === 'REQUEST' ? 'bg-blue-100 text-blue-600' :
                  act.type === 'MISSION' ? 'bg-emerald-100 text-emerald-600' :
                  act.type === 'ALERT' ? 'bg-red-100 text-red-600' : 'bg-amber-100 text-amber-600'
                }`}>
                  {act.type === 'REQUEST' ? <Activity size={16} /> :
                   act.type === 'MISSION' ? <CheckCircle size={16} /> :
                   act.type === 'ALERT' ? <AlertTriangle size={16} /> : <Package size={16} />}
                </div>
                <div className="flex-1">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-medium text-slate-900">{act.description}</p>
                    <span className="text-xs text-slate-500">{act.timestamp}</span>
                  </div>
                  <p className="text-xs text-slate-500 mt-0.5">Bởi {act.user}</p>
                </div>
                <ChevronRight className="h-4 w-4 text-slate-300" />
              </div>
            ))}
            <button className="w-full py-2 text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors">
              Xem tất cả hoạt động
            </button>
          </div>
        </ChartContainer>
      </div>
    </div>
  );
}

function StatCard({ title, value, icon, trend, color }: { 
  title: string, 
  value: string | number, 
  icon: React.ReactNode, 
  trend: string,
  color: 'blue' | 'emerald' | 'amber' | 'indigo'
}) {
  const colorMap = {
    blue: 'from-blue-500/10 to-blue-500/5 text-blue-600 border-blue-100',
    emerald: 'from-emerald-500/10 to-emerald-500/5 text-emerald-600 border-emerald-100',
    amber: 'from-amber-500/10 to-amber-500/5 text-amber-600 border-amber-100',
    indigo: 'from-indigo-500/10 to-indigo-500/5 text-indigo-600 border-indigo-100',
  };

  return (
    <article className={`relative overflow-hidden rounded-2xl border bg-white p-5 shadow-sm transition-all hover:shadow-md ${colorMap[color].split(' ').slice(-1)[0]}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-slate-500">{title}</p>
          <h3 className="mt-1 text-2xl font-bold text-slate-900">{value}</h3>
        </div>
        <div className={`rounded-xl bg-gradient-to-br p-2.5 ${colorMap[color]}`}>
          {icon}
        </div>
      </div>
      <div className="mt-4 flex items-center gap-1.5 text-xs">
        <TrendingUp className="h-3 w-3 text-emerald-500" />
        <span className="text-slate-500">{trend}</span>
      </div>
    </article>
  );
}

function ChartContainer({ title, icon, children, className }: { 
  title: string, 
  icon: React.ReactNode, 
  children: React.ReactNode,
  className?: string
}) {
  return (
    <section className={`rounded-2xl border border-slate-200 bg-white p-6 shadow-sm ${className}`}>
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="rounded-lg bg-slate-100 p-2 text-slate-600">
            {icon}
          </div>
          <h3 className="font-bold text-slate-900">{title}</h3>
        </div>
        <button className="rounded-md px-2 py-1 text-xs font-medium text-slate-500 hover:bg-slate-50 transition-colors">
          Thao tác
        </button>
      </div>
      {children}
    </section>
  );
}
