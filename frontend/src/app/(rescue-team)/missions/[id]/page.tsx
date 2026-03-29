'use client';

import { useParams } from 'next/navigation';
import { useMissions } from '@/features/rescue-team/hooks/useMissions';
import { Button } from '@/shared/components/ui/button';
import { useState } from 'react';
import { toast } from 'sonner';

export default function MissionDetailPage() {
  const { id } = useParams();
  const { missions, loading } = useMissions();
  const mission = missions.find(m => m.id === id);
  const [resultNote, setResultNote] = useState('');
  const [isStarted, setIsStarted] = useState(false);

  if (loading) return <div className="text-center py-10">Đang tải...</div>;
  if (!mission) return <div className="text-center py-10">Không tìm thấy nhiệm vụ</div>;

  const handleStart = () => {
    setIsStarted(true);
    // TODO: bắt đầu gửi GPS và cập nhật trạng thái
    toast.success('Đã bắt đầu nhiệm vụ');
  };

  const handleComplete = () => {
    // TODO: gửi kết quả và dừng GPS
    toast.success('Hoàn thành nhiệm vụ');
  };

  return (
    <div className="max-w-2xl mx-auto py-8 px-4">
      <h1 className="text-2xl font-bold">{mission.title}</h1>
      <p className="mt-2 text-gray-600">{mission.description}</p>
      <p className="mt-1">Địa chỉ: {mission.location.address}</p>
      {mission.peopleCount && <p>Số người: {mission.peopleCount}</p>}

      <div className="mt-6 space-y-4">
        {!isStarted && mission.status === 'active' && (
          <Button onClick={handleStart}>Bắt đầu</Button>
        )}
        {isStarted && (
          <>
            <textarea
              className="w-full border rounded p-2"
              rows={3}
              placeholder="Ghi chú kết quả..."
              value={resultNote}
              onChange={(e) => setResultNote(e.target.value)}
            />
            <Button onClick={handleComplete}>Hoàn thành</Button>
          </>
        )}
      </div>
    </div>
  );
}