"use client";

import * as React from "react";
import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { Search, X, Calendar as CalendarIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { type RequestStatus, type UrgencyLevel } from "../types/request.types";

export function RequestFilterBar() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  // Get current filter values from URL
  const currentStatus = searchParams.get("status") || "";
  const currentUrgency = searchParams.get("urgencyLevel") || "";
  const currentSearch = searchParams.get("search") || "";
  const currentDateFrom = searchParams.get("fromDate") || "";
  const currentDateTo = searchParams.get("toDate") || "";

  const [search, setSearch] = React.useState(currentSearch);

  // Update local search state when URL changes
  React.useEffect(() => {
    setSearch(currentSearch);
  }, [currentSearch]);

  const updateFilters = (newFilters: Record<string, string | null>) => {
    const params = new URLSearchParams(searchParams.toString());
    
    Object.entries(newFilters).forEach(([key, value]) => {
      if (value) {
        params.set(key, value);
      } else {
        params.delete(key);
      }
    });

    // Reset page when filters change
    params.delete("page");
    
    router.push(`${pathname}?${params.toString()}`);
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateFilters({ search });
  };

  const clearFilters = () => {
    router.push(pathname);
    setSearch("");
  };

  const hasFilters = currentStatus || currentUrgency || currentSearch || currentDateFrom || currentDateTo;

  return (
    <div className="space-y-4 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm mb-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
        {/* Search Input */}
        <form onSubmit={handleSearchSubmit} className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            placeholder="Tìm kiếm theo mô tả hoặc địa chỉ..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 pr-4 h-11 border-slate-200 focus:ring-brand-500 rounded-xl bg-slate-50/50"
          />
          {search && (
            <button 
              type="button" 
              onClick={() => { setSearch(""); updateFilters({ search: null }); }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </form>

        {/* Filters Group */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Status Filter */}
          <div className="relative group">
            <select
              value={currentStatus}
              onChange={(e) => updateFilters({ status: e.target.value })}
              className="h-11 appearance-none rounded-xl border border-slate-200 bg-white pl-4 pr-10 text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-brand-500 transition-all cursor-pointer hover:border-brand-300"
            >
              <option value="">Mọi trạng thái</option>
              <option value="PENDING">Chờ xử lý</option>
              <option value="VERIFIED">Đã xác minh</option>
              <option value="ASSIGNED">Đã phân công</option>
              <option value="COMPLETED">Hoàn thành</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
              <svg className="h-4 w-4 fill-current" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
              </svg>
            </div>
          </div>

          {/* Urgency Filter */}
          <div className="relative">
            <select
              value={currentUrgency}
              onChange={(e) => updateFilters({ urgencyLevel: e.target.value })}
              className="h-11 appearance-none rounded-xl border border-slate-200 bg-white pl-4 pr-10 text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-brand-500 transition-all cursor-pointer hover:border-brand-300"
            >
              <option value="">Mọi mức độ</option>
              <option value="CRITICAL">Khẩn cấp 🚨</option>
              <option value="HIGH">Cao</option>
              <option value="MEDIUM">Trung bình</option>
              <option value="LOW">Thấp</option>
            </select>
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
              <svg className="h-4 w-4 fill-current" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
              </svg>
            </div>
          </div>

          {/* Date Range */}
          <div className="flex items-center gap-1 rounded-xl border border-slate-200 px-3 bg-white h-11 hover:border-brand-300 transition-all">
            <CalendarIcon className="h-4 w-4 text-slate-400" />
            <input
              type="date"
              value={currentDateFrom}
              onChange={(e) => updateFilters({ fromDate: e.target.value })}
              className="h-full bg-transparent text-xs font-bold text-slate-700 outline-none border-none focus:ring-0 cursor-pointer"
            />
            <span className="text-slate-300 mx-1 font-bold">→</span>
            <input
              type="date"
              value={currentDateTo}
              onChange={(e) => updateFilters({ toDate: e.target.value })}
              className="h-full bg-transparent text-xs font-bold text-slate-700 outline-none border-none focus:ring-0 cursor-pointer"
            />
          </div>

          {hasFilters && (
            <Button
              variant="ghost"
              onClick={clearFilters}
              className="h-11 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-xl font-bold px-4 transition-all"
            >
              <X className="mr-2 h-4 w-4" />
              Xóa bộ lọc
            </Button>
          )}
        </div>
      </div>

      {/* Active Filter Summary */}
      {hasFilters && (
        <div className="flex flex-wrap items-center gap-2 pt-3 border-t border-slate-100">
          <span className="text-[10px] font-black uppercase tracking-widest text-slate-400 mr-2">Kết quả đang lọc theo:</span>
          {currentSearch && (
            <Badge className="bg-brand-50 text-brand-700 border-brand-100 px-3 py-1">
              Từ khóa: {currentSearch}
            </Badge>
          )}
          {currentStatus && (
            <Badge className="bg-indigo-50 text-indigo-700 border-indigo-100 px-3 py-1">
              Trạng thái: {currentStatus}
            </Badge>
          )}
          {currentUrgency && (
            <Badge className="bg-orange-50 text-orange-700 border-orange-100 px-3 py-1">
              Độ khẩn: {currentUrgency}
            </Badge>
          )}
          {(currentDateFrom || currentDateTo) && (
            <Badge className="bg-slate-100 text-slate-600 border-slate-200 px-3 py-1">
              Thời gian: {currentDateFrom || '...'} → {currentDateTo || '...'}
            </Badge>
          )}
        </div>
      )}
    </div>
  );
}
