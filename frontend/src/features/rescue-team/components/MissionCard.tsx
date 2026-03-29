import { Mission } from '../hooks/useMissions';
import { Button } from '@/shared/components/ui/button';
import Link from 'next/link';

interface MissionCardProps {
  mission: Mission;
  isActive: boolean;
}

export function MissionCard({ mission, isActive }: MissionCardProps) {
  return (
    <div className={`border rounded-lg p-4 ${isActive ? 'border-green-500 bg-green-50' : 'bg-white'}`}>
      <div className="flex justify-between items-start">
        <div>
          <h3 className="font-semibold text-lg">{mission.title}</h3>
          <p className="text-gray-600 text-sm mt-1">{mission.location.address}</p>
          {mission.peopleCount && (
            <p className="text-sm mt-2">Số người: {mission.peopleCount}</p>
          )}
        </div>
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          mission.status === 'active' ? 'bg-green-100 text-green-800' :
          mission.status === 'completed' ? 'bg-gray-100 text-gray-800' :
          'bg-yellow-100 text-yellow-800'
        }`}>
          {mission.status === 'active' ? 'Đang thực hiện' :
           mission.status === 'completed' ? 'Hoàn thành' : 'Chờ xử lý'}
        </span>
      </div>
      <div className="mt-4 flex justify-end">
        <Button variant="outline" asChild>
          <Link href={`/missions/${mission.id}`}>Chi tiết</Link>
        </Button>
      </div>
    </div>
  );
}