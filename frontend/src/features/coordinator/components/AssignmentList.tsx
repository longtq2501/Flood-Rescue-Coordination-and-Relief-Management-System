'use client';

import { useState } from 'react';
import { Assignment } from '../types';
import { Badge } from '@/shared/components/ui/badge';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import { Search } from 'lucide-react';

interface AssignmentListProps {
  assignments: Assignment[];
}

const STATUS_LABELS: Record<Assignment['status'], string> = {
  assigned: 'Đã phân công',
  en_route: 'Đang đến',
  on_site: 'Đang xử lý',
  completed: 'Hoàn thành',
};

export function AssignmentList({ assignments }: AssignmentListProps) {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<Assignment['status'] | 'all'>('all');

  const filtered = assignments.filter(a => {
    const matchSearch = a.id.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === 'all' || a.status === statusFilter;
    return matchSearch && matchStatus;
  });

  return (
    <div className="space-y-4">
      <div className="flex gap-4 items-center">
        <div className="relative flex-1">
          <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Tìm kiếm theo mã..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-8"
          />
        </div>
        <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as Assignment['status'] | 'all')}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-md border">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="py-3 px-4 text-left">Mã</th>
              <th className="py-3 px-4 text-left">Mã yêu cầu</th>
              <th className="py-3 px-4 text-left">Mã đội</th>
              <th className="py-3 px-4 text-left">Trạng thái</th>
              <th className="py-3 px-4 text-left">Thời gian phân công</th>
              <th className="py-3 px-4 text-left">Dự kiến đến</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-8 text-center text-muted-foreground">
                  Không có phân công nào
                </td>
              </tr>
            ) : (
              filtered.map(a => (
                <tr key={a.id} className="border-t hover:bg-muted/50">
                  <td className="py-3 px-4">{a.id}</td>
                  <td className="py-3 px-4">{a.requestId}</td>
                  <td className="py-3 px-4">{a.teamId}</td>
                  <td className="py-3 px-4">
                    <Badge variant="outline">{STATUS_LABELS[a.status]}</Badge>
                  </td>
                  <td className="py-3 px-4">{new Date(a.assignedAt).toLocaleString('vi-VN')}</td>
                  <td className="py-3 px-4">{new Date(a.estimatedArrival).toLocaleString('vi-VN')}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}