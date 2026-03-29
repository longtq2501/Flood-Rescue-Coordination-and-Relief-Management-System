'use client';

import { useParams } from 'next/navigation';
import { useCitizenRequests } from '@/features/citizen/hooks/useCitizenRequests';
import { RequestStatusBadge } from '@/features/citizen/components/RequestStatusBadge';
import { RequestTimeline } from '@/features/citizen/components/RequestTimeline';
import { Button } from '@/shared/components/ui/button';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { CitizenRequest } from '@/features/citizen/types';

export default function RequestDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const { requests, cancelRequest, confirmCompletion, loading } = useCitizenRequests();
  const [request, setRequest] = useState<CitizenRequest | null>(null);

  useEffect(() => {
    const found = requests.find(r => r.id === id);
    setRequest(found);
  }, [requests, id]);

  const handleCancel = async () => {
    if (confirm('Bạn có chắc muốn hủy yêu cầu này không?')) {
      await cancelRequest(id as string);
      router.push('/requests');
    }
  };

  const handleConfirm = async () => {
    await confirmCompletion(id as string);
  };

  if (loading) return <div className="text-center py-10">Đang tải...</div>;
  if (!request) return <div className="text-center py-10">Không tìm thấy yêu cầu</div>;

  const isPending = request.status === 'pending' || request.status === 'verified';
  const isCompleted = request.status === 'completed';

  return (
    <div className="max-w-3xl mx-auto py-8">
      <div className="border rounded-lg p-6">
        <div className="flex justify-between items-start mb-4">
          <h1 className="text-2xl font-bold">{request.title}</h1>
          <RequestStatusBadge status={request.status} />
        </div>

        <div className="space-y-4">
          <div>
            <h3 className="font-medium">Địa điểm</h3>
            <p>{request.location.address}</p>
          </div>
          <div>
            <h3 className="font-medium">Mô tả</h3>
            <p className="whitespace-pre-wrap">{request.description}</p>
          </div>
          {request.peopleCount && (
            <div>
              <h3 className="font-medium">Số người</h3>
              <p>{request.peopleCount}</p>
            </div>
          )}
          {request.assignedTeam && (
            <div>
              <h3 className="font-medium">Đội cứu hộ</h3>
              <p>{request.assignedTeam.name}</p>
              {request.assignedTeam.eta && <p className="text-sm text-green-600">Dự kiến: {request.assignedTeam.eta}</p>}
            </div>
          )}

          <RequestTimeline request={request} />
        </div>

        <div className="mt-8 flex gap-3">
          {isPending && (
            <Button variant="destructive" onClick={handleCancel}>
              Hủy yêu cầu
            </Button>
          )}
          {isCompleted && (
            <Button onClick={handleConfirm}>
              Xác nhận đã hoàn thành
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}