"use client";

import { useState, Suspense } from "react";
import { CoordinatorBoard } from "@/features/dispatch/components/coordinator-board";
import { AnalyticsDashboard } from "@/features/report/components/analytics-dashboard";
import { Activity, LayoutDashboard } from "lucide-react";

export default function CoordinatorDashboardPage() {
  const [activeTab, setActiveTab] = useState<"ops" | "analytics">("ops");

  return (
    <div className="space-y-6">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Bảng điều phối cứu trợ</h1>
          <p className="text-slate-500">Quản lý xác minh và phân công nhiệm vụ cứu hộ</p>
        </div>
        
        <div className="flex rounded-xl bg-slate-100 p-1">
          <button
            onClick={() => setActiveTab("ops")}
            className={`flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-all ${
              activeTab === "ops" 
                ? "bg-white text-blue-600 shadow-sm" 
                : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <Activity size={16} />
            Điều hành
          </button>
          <button
            onClick={() => setActiveTab("analytics")}
            className={`flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-medium transition-all ${
              activeTab === "analytics" 
                ? "bg-white text-blue-600 shadow-sm" 
                : "text-slate-600 hover:text-slate-900"
            }`}
          >
            <LayoutDashboard size={16} />
            Phân tích
          </button>
        </div>
      </div>

      {activeTab === "ops" ? (
        <Suspense fallback={<div className="h-64 animate-pulse bg-slate-50 rounded-2xl border border-slate-100" />}>
          <CoordinatorBoard />
        </Suspense>
      ) : <AnalyticsDashboard />}
    </div>
  );
}
