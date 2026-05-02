'use client';

import { Suspense } from 'react';
import { RequestTable } from '@/features/coordinator/components/RequestTable';
import { AssignModal } from '@/features/coordinator/components/AssignModal';
import { useRequests } from '@/features/coordinator/hooks/useRequests';
import { Request } from '@/features/coordinator/types';
import { RequestFilterParams } from '@/features/request/types/request.types';
import { PageParams } from '@/shared/types/api.types';

export default function DashboardPage() {
  const [selectedRequest, setSelectedRequest] = useState<Request | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [filters, setFilters] = useState<RequestFilterParams & PageParams>({ page: 0, size: 10 });

  const { requests, loading, totalElements, totalPages, verifyRequest } = useRequests(filters);

  const handleVerify = (requestId: number) => {
    verifyRequest(requestId);
  };

  const handleOpenAssign = (request: Request) => {
    setSelectedRequest(request);
    setModalOpen(true);
  };

  const handleAssign = async (requestId: number, teamId: string, vehicleId: string) => {
    console.log('Assign:', { requestId, teamId, vehicleId });
    // TODO: Gọi API assign
  };

  const handleFiltersChange = (newFilters: RequestFilterParams & PageParams) => {
    setFilters(newFilters);
  };

  return (
    <div className="container mx-auto py-8 px-4">
      <h1 className="text-3xl font-bold mb-8">Dashboard Điều phối</h1>
      <Suspense fallback={<div>Đang tải...</div>}>
        <RequestTable
          requests={requests}
          onVerify={handleVerify}
          onOpenAssign={handleOpenAssign}
          loading={loading}
          filters={filters}
          onFiltersChange={handleFiltersChange}
          totalElements={totalElements}
          totalPages={totalPages}
        />
      </Suspense>
      <AssignModal
        open={modalOpen}
        onOpenChange={setModalOpen}
        request={selectedRequest}
        onAssign={handleAssign}
      />
    </div>
  );
}