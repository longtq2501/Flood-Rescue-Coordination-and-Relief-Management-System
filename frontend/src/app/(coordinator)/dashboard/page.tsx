'use client';

import { useState } from 'react';
import { RequestTable } from '@/features/coordinator/components/RequestTable';
import { AssignModal } from '@/features/coordinator/components/AssignModal';
import { Request } from '@/features/coordinator/types';

const MOCK_REQUESTS: Request[] = [
  {
    id: '1',
    title: 'Xe máy chết máy',
    description: 'Xe không nổ được máy',
    location: '123 Nguyễn Văn Linh, Quận 7',
    urgency: 'high',
    status: 'pending',
    createdAt: new Date().toISOString(),
    customerName: 'Nguyễn Văn A',
    customerPhone: '0901234567',
  },
  {
    id: '2',
    title: 'Tai nạn giao thông',
    description: 'Xe máy va chạm với ô tô',
    location: 'Giao lộ Nguyễn Thị Thập - Lê Văn Lương',
    urgency: 'critical',
    status: 'assigned',
    createdAt: new Date().toISOString(),
    customerName: 'Trần Thị B',
    customerPhone: '0912345678',
  },
  {
    id: '3',
    title: 'Hết xăng',
    description: 'Xe hết xăng giữa đường',
    location: 'Đường Nguyễn Hữu Thọ, Quận 7',
    urgency: 'low',
    status: 'pending',
    createdAt: new Date().toISOString(),
    customerName: 'Lê Văn C',
    customerPhone: '0923456789',
  },
];

export default function DashboardPage() {
  const [selectedRequest, setSelectedRequest] = useState<Request | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  const handleVerify = (requestId: string) => {
    alert(`Xác thực yêu cầu ${requestId}`);
  };

  const handleOpenAssign = (request: Request) => {
    setSelectedRequest(request);
    setModalOpen(true);
  };

  const handleAssign = async (requestId: string, teamId: string, vehicleId: string) => {
    console.log('Assign:', { requestId, teamId, vehicleId });
    // TODO: Gọi API assign
  };

  return (
    <div className="container mx-auto py-8 px-4">
      <h1 className="text-3xl font-bold mb-8">Dashboard Điều phối</h1>
      <RequestTable
        requests={MOCK_REQUESTS}
        onVerify={handleVerify}
        onOpenAssign={handleOpenAssign}
      />
      <AssignModal
        open={modalOpen}
        onOpenChange={setModalOpen}
        request={selectedRequest}
        onAssign={handleAssign}
      />
    </div>
  );
}