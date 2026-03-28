'use client';

import { useState } from 'react';
import dynamic from 'next/dynamic';
const TeamMap = dynamic(() => import('@/features/coordinator/components/TeamMap').then(mod => mod.TeamMap), {
  ssr: false,
  loading: () => <div className="w-full h-[600px] bg-gray-100 animate-pulse rounded-lg flex items-center justify-center text-gray-500">Đang tải bản đồ...</div>
});
import { Team } from '@/features/coordinator/types';

const MOCK_TEAMS: Team[] = [
  {
    id: '1',
    name: 'Đội cứu hộ 1',
    status: 'AVAILABLE',
    location: { lat: 10.8231, lng: 106.6297 },
    members: 4,
    vehicle: 'Xe cứu thương',
  },
  {
    id: '2',
    name: 'Đội cứu hộ 2',
    status: 'BUSY',
    location: { lat: 10.785, lng: 106.694 },
    members: 3,
    vehicle: 'Xe cứu hộ',
  },
  {
    id: '3',
    name: 'Đội cứu hộ 3',
    status: 'OFFLINE',
    location: { lat: 10.842, lng: 106.675 },
    members: 2,
    vehicle: 'Xuồng máy',
  },
];

export default function MapPage() {
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);

  return (
    <div className="container mx-auto py-8 px-4">
      <h1 className="text-3xl font-bold mb-8">Bản đồ đội cứu hộ</h1>
      <TeamMap teams={MOCK_TEAMS} onTeamClick={setSelectedTeam} />
      {selectedTeam && (
        <div className="mt-4 p-4 border rounded bg-gray-50">
          <h2 className="text-xl font-semibold">{selectedTeam.name}</h2>
          <p>Trạng thái: {selectedTeam.status}</p>
          <p>Thành viên: {selectedTeam.members}</p>
          <p>Phương tiện: {selectedTeam.vehicle}</p>
        </div>
      )}
    </div>
  );
}