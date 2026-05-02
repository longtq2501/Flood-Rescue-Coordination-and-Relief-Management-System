'use client';

import { useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { VehicleResponse, VehicleStatus, VehicleType } from '../types/resource.types';
import { PageParams } from '@/shared/types/api.types';
import { Input } from '@/shared/components/ui/input';
import { Badge } from '@/shared/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Search } from 'lucide-react';

interface VehicleTableProps {
  vehicles: VehicleResponse[];
  loading?: boolean;
  filters: PageParams & { status?: VehicleStatus; type?: VehicleType; search?: string };
  onFiltersChange: (filters: PageParams & { status?: VehicleStatus; type?: VehicleType; search?: string }) => void;
  totalElements: number;
  totalPages: number;
}

const STATUS_LABELS: Record<VehicleStatus, string> = {
  AVAILABLE: 'Sẵn sàng',
  IN_USE: 'Đang sử dụng',
  MAINTENANCE: 'Bảo trì',
  OFFLINE: 'Ngoại tuyến',
};

const TYPE_LABELS: Record<VehicleType, string> = {
  BOAT: 'Thuyền',
  TRUCK: 'Xe tải',
  HELICOPTER: 'Trực thăng',
  AMBULANCE: 'Xe cứu thương',
  OTHER: 'Khác',
};

export function VehicleTable({ vehicles, loading, filters, onFiltersChange, totalElements, totalPages }: VehicleTableProps) {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [statusFilter, setStatusFilter] = useState<VehicleStatus | 'all'>(searchParams.get('status') as VehicleStatus || 'all');
  const [typeFilter, setTypeFilter] = useState<VehicleType | 'all'>(searchParams.get('type') as VehicleType || 'all');

  useEffect(() => {
    const timer = setTimeout(() => {
      onFiltersChange({ ...filters, search });
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    onFiltersChange({
      ...filters,
      status: statusFilter === 'all' ? undefined : statusFilter,
      type: typeFilter === 'all' ? undefined : typeFilter,
    });
  }, [statusFilter, typeFilter]);

  useEffect(() => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    if (statusFilter !== 'all') params.set('status', statusFilter);
    if (typeFilter !== 'all') params.set('type', typeFilter);
    router.replace(`?${params.toString()}`, { scroll: false });
  }, [search, statusFilter, typeFilter]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm kiếm biển số hoặc loại..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-8"
          />
        </div>
        <Select value={statusFilter} onValueChange={(value) => setStatusFilter(value as VehicleStatus | 'all')}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả trạng thái</SelectItem>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={typeFilter} onValueChange={(value) => setTypeFilter(value as VehicleType | 'all')}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Loại" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả loại</SelectItem>
            {Object.entries(TYPE_LABELS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="py-3 px-4 text-left font-medium">Biển số</th>
              <th className="py-3 px-4 text-left font-medium">Loại</th>
              <th className="py-3 px-4 text-left font-medium">Trạng thái</th>
              <th className="py-3 px-4 text-left font-medium">Vị trí</th>
              <th className="py-3 px-4 text-left font-medium">Đội</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted-foreground">Đang tải...</td>
              </tr>
            ) : vehicles.length === 0 ? (
              <tr>
                <td colSpan={5} className="py-8 text-center text-muted-foreground">Không có phương tiện nào</td>
              </tr>
            ) : (
              vehicles.map((vehicle) => (
                <tr key={vehicle.id} className="border-t hover:bg-muted/50">
                  <td className="py-3 px-4 font-medium">{vehicle.plateNumber}</td>
                  <td className="py-3 px-4">{TYPE_LABELS[vehicle.type]}</td>
                  <td className="py-3 px-4"><Badge variant="outline">{STATUS_LABELS[vehicle.status]}</Badge></td>
                  <td className="py-3 px-4">{vehicle.currentLat && vehicle.currentLng ? `${vehicle.currentLat}, ${vehicle.currentLng}` : 'N/A'}</td>
                  <td className="py-3 px-4">{vehicle.assignedTeamId || 'N/A'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between mt-4">
        <div className="text-sm text-muted-foreground">
          Hiển thị {vehicles.length} / {totalElements} phương tiện
        </div>
        <div className="flex items-center space-x-2">
          <button
            className="rounded border px-3 py-1 text-sm"
            onClick={() => onFiltersChange({ ...filters, page: Math.max(0, filters.page! - 1) })}
            disabled={filters.page === 0}
          >
            Trước
          </button>
          <span className="text-sm">Trang {filters.page! + 1} / {totalPages}</span>
          <button
            className="rounded border px-3 py-1 text-sm"
            onClick={() => onFiltersChange({ ...filters, page: filters.page! + 1 })}
            disabled={filters.page! + 1 >= totalPages}
          >
            Sau
          </button>
        </div>
      </div>
    </div>
  );
}
