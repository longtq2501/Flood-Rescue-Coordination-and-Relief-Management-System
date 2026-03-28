'use client';

import { useCitizenRequests } from '@/features/citizen/hooks/useCitizenRequests';
import { RequestCard } from '@/features/citizen/components/RequestCard';
import { RequestStatusBadge } from '@/features/citizen/components/RequestStatusBadge';
import { Button } from '@/shared/components/ui/button';
import Link from 'next/link';
import { useState } from 'react';

export default function RequestsPage() {
  const { requests, loading } = useCitizenRequests();
  const [statusFilter, setStatusFilter] = useState<string>('all');

  const filteredRequests = requests.filter(req =>
    statusFilter === 'all' ? true : req.status === statusFilter
  );

  const statuses = ['all', 'pending', 'assigned', 'in_progress', 'completed', 'cancelled'];

  if (loading) return <div className="text-center py-10">Đang tải...</div>;

  return (
    <div className="max-w-4xl mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Yêu cầu của tôi</h1>
        <Button asChild>
          <Link href="/requests/new">+ Tạo yêu cầu mới</Link>
        </Button>
      </div>

      <div className="flex gap-2 mb-6 overflow-x-auto pb-2">
        {statuses.map(status => (
          <button
            key={status}
            onClick={() => setStatusFilter(status)}
            className={`px-3 py-1 rounded-full text-sm ${
              statusFilter === status
                ? 'bg-red-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {status === 'all' ? 'Tất cả' : status}
          </button>
        ))}
      </div>

      {filteredRequests.length === 0 ? (
        <div className="text-center py-10 text-gray-500">
          Bạn chưa có yêu cầu nào.
        </div>
      ) : (
        <div className="space-y-4">
          {filteredRequests.map(req => (
            <div key={req.id} className="border rounded-lg p-4 hover:shadow transition">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="font-semibold text-lg">{req.title}</h3>
                  <p className="text-gray-600 text-sm mt-1">{req.location.address}</p>
                  <p className="text-gray-500 text-sm mt-2 line-clamp-2">{req.description}</p>
                </div>
                <RequestStatusBadge status={req.status} />
              </div>
              <div className="mt-4 flex justify-end">
                <Button variant="outline" asChild>
                  <Link href={`/requests/${req.id}`}>Xem chi tiết</Link>
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}