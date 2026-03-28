'use client';

import { AssignmentList } from '@/features/coordinator/components/AssignmentList';
import { Assignment } from '@/features/coordinator/types';

const MOCK_ASSIGNMENTS: Assignment[] = [
  {
    id: 'A1',
    requestId: '1',
    teamId: '1',
    status: 'en_route',
    assignedAt: new Date().toISOString(),
    estimatedArrival: new Date(Date.now() + 15 * 60000).toISOString(),
  },
  {
    id: 'A2',
    requestId: '2',
    teamId: '2',
    status: 'on_site',
    assignedAt: new Date(Date.now() - 30 * 60000).toISOString(),
    estimatedArrival: new Date().toISOString(),
  },
  {
    id: 'A3',
    requestId: '3',
    teamId: '3',
    status: 'completed',
    assignedAt: new Date(Date.now() - 2 * 3600000).toISOString(),
    estimatedArrival: new Date(Date.now() - 1 * 3600000).toISOString(),
  },
];

export default function AssignmentsPage() {
  return (
    <div className="container mx-auto py-8 px-4">
      <h1 className="text-3xl font-bold mb-8">Danh sách phân công</h1>
      <AssignmentList assignments={MOCK_ASSIGNMENTS} />
    </div>
  );
}