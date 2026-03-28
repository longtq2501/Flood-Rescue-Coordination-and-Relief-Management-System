'use client';

import { useParams } from 'next/navigation';
import { useCitizenRequests } from '@/features/citizen/hooks/useCitizenRequests';
import { RequestStatusBadge } from '@/features/citizen/components/RequestStatusBadge';
import { Button } from '@/shared/components/ui/button';
import Link from 'next/link';

export default function RequestDetailPage() {
  const { id } = useParams();
  const { requests, loading } = useCitizenRequests();
  
  const request = requests.find(r => r.id === id);

  if (loading) return <div className="text-center py-10">Đang tải...</div>;
  if (!request) return <div className="text-center py-10">Không tìm thấy yêu cầu.</div>;

  return (
    <div className="max-w-2xl mx-auto py-8">
      <div className="mb-6">
        <Button variant="outline" asChild>
          <Link href="/requests">← Quay lại danh sách</Link>
        </Button>
      </div>
      
      <div className="bg-white border rounded-xl p-6 shadow-sm">
        <div className="flex justify-between items-start mb-4">
          <h1 className="text-3xl font-bold">{request.title}</h1>
          <RequestStatusBadge status={request.status} />
        </div>
        
        <div className="space-y-4 text-gray-700">
          <div>
            <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Mô tả</h3>
            <p className="mt-1">{request.description}</p>
          </div>
          
          <div>
            <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Địa chỉ</h3>
            <p className="mt-1">{request.location.address}</p>
          </div>
          
          <div className="flex gap-8">
            <div>
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Mức độ</h3>
              <p className="mt-1 capitalize">{request.urgency}</p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Số người</h3>
              <p className="mt-1">{request.peopleCount || 'N/A'}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
