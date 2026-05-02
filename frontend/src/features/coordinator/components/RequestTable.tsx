'use client';

import { useState, useMemo, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { Request, Urgency, RequestStatus } from '../types';
import { URGENCY_COLORS, URGENCY_LABELS, REQUEST_STATUS } from '../constants';
import { RequestFilterParams } from '@/features/request/types/request.types';
import { PageParams } from '@/shared/types/api.types';
import { Input } from '@/shared/components/ui/input';
import { Button } from '@/shared/components/ui/button';
import { Badge } from '@/shared/components/ui/badge';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Search, Eye } from 'lucide-react';

interface RequestTableProps {
  requests: Request[];
  onVerify: (requestId: number) => void;
  onOpenAssign: (request: Request) => void;
  loading?: boolean;
  filters: RequestFilterParams & PageParams;
  onFiltersChange: (filters: RequestFilterParams & PageParams) => void;
  totalElements: number;
  totalPages: number;
}

export function RequestTable({ requests, onVerify, onOpenAssign, loading, filters, onFiltersChange, totalElements, totalPages }: RequestTableProps) {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [debouncedSearch, setDebouncedSearch] = useState(searchParams.get('search') || '');
  const [statusFilter, setStatusFilter] = useState<RequestStatus | 'all'>(searchParams.get('status') as RequestStatus || 'all');
  const [urgencyFilter, setUrgencyFilter] = useState<Urgency | 'all'>(searchParams.get('urgency') as Urgency || 'all');

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      onFiltersChange({ ...filters, search });
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    onFiltersChange({ ...filters, status: statusFilter === 'all' ? undefined : statusFilter, urgencyLevel: urgencyFilter === 'all' ? undefined : urgencyFilter });
  }, [statusFilter, urgencyFilter]);

  const updateURL = () => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    if (statusFilter !== 'all') params.set('status', statusFilter);
    if (urgencyFilter !== 'all') params.set('urgency', urgencyFilter);
    router.replace(`?${params.toString()}`, { scroll: false });
  };

  useEffect(() => {
    updateURL();
  }, [search, statusFilter, urgencyFilter]);

  const filteredRequests = useMemo(() => {
    return requests.filter((req) => {
      const matchesSearch =
        req.description.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
        req.addressText?.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
        req.citizenId.toString().includes(debouncedSearch);
      const matchesStatus = statusFilter === 'all' || req.status === statusFilter;
      const matchesUrgency = urgencyFilter === 'all' || req.urgencyLevel === urgencyFilter;
      return matchesSearch && matchesStatus && matchesUrgency;
    });
  }, [requests, debouncedSearch, statusFilter, urgencyFilter]);

  const formattedDates = useMemo(() => {
    const dates: Record<number, string> = {};
    filteredRequests.forEach(req => {
      dates[req.id] = new Date(req.createdAt).toLocaleString('vi-VN');
    });
    return dates;
  }, [filteredRequests]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm kiếm yêu cầu..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-8"
          />
        </div>
        <Select
          value={statusFilter}
          onValueChange={(value) => setStatusFilter(value as RequestStatus | 'all')}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả trạng thái</SelectItem>
            {Object.entries(REQUEST_STATUS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select
          value={urgencyFilter}
          onValueChange={(value) => setUrgencyFilter(value as Urgency | 'all')}
        >
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Mức độ" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả mức độ</SelectItem>
            {Object.entries(URGENCY_LABELS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="py-3 px-4 text-left font-medium">Mức độ</th>
              <th className="py-3 px-4 text-left font-medium">Mô tả</th>
              <th className="py-3 px-4 text-left font-medium">Công dân</th>
              <th className="py-3 px-4 text-left font-medium">Vị trí</th>
              <th className="py-3 px-4 text-left font-medium">Trạng thái</th>
              <th className="py-3 px-4 text-left font-medium">Thời gian</th>
              <th className="py-3 px-4 text-left font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={7} className="py-8 text-center text-muted-foreground">
                  Đang tải...
                </td>
              </tr>
            ) : filteredRequests.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-8 text-center text-muted-foreground">
                  Không có yêu cầu nào
                </td>
              </tr>
            ) : (
              filteredRequests.map((request) => (
                <tr key={request.id} className="border-t hover:bg-muted/50">
                  <td className="py-3 px-4">
                    <Badge className={URGENCY_COLORS[request.urgencyLevel]}>
                      {URGENCY_LABELS[request.urgencyLevel]}
                    </Badge>
                  </td>
                  <td className="py-3 px-4 font-medium">{request.description}</td>
                  <td className="py-3 px-4">
                    <div>ID: {request.citizenId}</div>
                    <div className="text-xs text-muted-foreground">{request.numPeople} người</div>
                  </td>
                  <td className="py-3 px-4">{request.addressText || `${request.lat}, ${request.lng}`}</td>
                  <td className="py-3 px-4">
                    <Badge variant="outline">{REQUEST_STATUS[request.status.toLowerCase() as keyof typeof REQUEST_STATUS]}</Badge>
                  </td>
                  <td className="py-3 px-4 text-muted-foreground">
                    {formattedDates[request.id] || ''}
                  </td>
                  <td className="py-3 px-4 space-x-2">
                    <Button size="sm" variant="outline" onClick={() => onVerify(request.id)}>
                      <Eye className="h-4 w-4 mr-1" />
                      Verify
                    </Button>
                    <Button
                      size="sm"
                      onClick={() => onOpenAssign(request)}
                      disabled={request.status !== 'PENDING'}
                    >
                      Phân công
                    </Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between mt-4">
        <div className="text-sm text-muted-foreground">
          Hiển thị {requests.length} / {totalElements} yêu cầu
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onFiltersChange({ ...filters, page: Math.max(0, filters.page! - 1) })}
            disabled={filters.page === 0}
          >
            Trước
          </Button>
          <span className="text-sm">
            Trang {filters.page! + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onFiltersChange({ ...filters, page: filters.page! + 1 })}
            disabled={filters.page! + 1 >= totalPages}
          >
            Sau
          </Button>
        </div>
      </div>
    </div>
  );
}