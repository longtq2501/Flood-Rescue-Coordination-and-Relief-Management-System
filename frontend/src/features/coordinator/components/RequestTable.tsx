'use client';

import { useState, useEffect } from 'react';
import { Request, Urgency, RequestStatus } from '../types';
import { URGENCY_COLORS, URGENCY_LABELS, REQUEST_STATUS } from '../constants';
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
  onVerify: (requestId: string) => void;
  onOpenAssign: (request: Request) => void;
}

export function RequestTable({ requests, onVerify, onOpenAssign }: RequestTableProps) {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<RequestStatus | 'all'>('all');
  const [urgencyFilter, setUrgencyFilter] = useState<Urgency | 'all'>('all');
  const [formattedDates, setFormattedDates] = useState<Record<string, string>>({});

  useEffect(() => {
    const dates: Record<string, string> = {};
    requests.forEach(req => {
      dates[req.id] = new Date(req.createdAt).toLocaleString('vi-VN');
    });
    setFormattedDates(dates);
  }, [requests]);

  const filteredRequests = requests.filter((req) => {
    const matchesSearch =
      req.title.toLowerCase().includes(search.toLowerCase()) ||
      req.customerName.toLowerCase().includes(search.toLowerCase()) ||
      req.location.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'all' || req.status === statusFilter;
    const matchesUrgency = urgencyFilter === 'all' || req.urgency === urgencyFilter;
    return matchesSearch && matchesStatus && matchesUrgency;
  });

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
              <th className="py-3 px-4 text-left font-medium">Yêu cầu</th>
              <th className="py-3 px-4 text-left font-medium">Khách hàng</th>
              <th className="py-3 px-4 text-left font-medium">Vị trí</th>
              <th className="py-3 px-4 text-left font-medium">Trạng thái</th>
              <th className="py-3 px-4 text-left font-medium">Thời gian</th>
              <th className="py-3 px-4 text-left font-medium">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filteredRequests.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-8 text-center text-muted-foreground">
                  Không có yêu cầu nào
                </td>
              </tr>
            ) : (
              filteredRequests.map((request) => (
                <tr key={request.id} className="border-t hover:bg-muted/50">
                  <td className="py-3 px-4">
                    <Badge className={URGENCY_COLORS[request.urgency]}>
                      {URGENCY_LABELS[request.urgency]}
                    </Badge>
                  </td>
                  <td className="py-3 px-4 font-medium">{request.title}</td>
                  <td className="py-3 px-4">
                    <div>{request.customerName}</div>
                    <div className="text-xs text-muted-foreground">{request.customerPhone}</div>
                  </td>
                  <td className="py-3 px-4">{request.location}</td>
                  <td className="py-3 px-4">
                    <Badge variant="outline">{REQUEST_STATUS[request.status]}</Badge>
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
                      disabled={request.status !== 'pending'}
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
    </div>
  );
}