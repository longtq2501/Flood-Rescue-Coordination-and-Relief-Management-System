import { CitizenRequest } from '../types';
import { RequestStatusBadge } from './RequestStatusBadge';
import { Button } from '@/shared/components/ui/button';
import Link from 'next/link';

interface RequestCardProps {
  request: CitizenRequest;
}

export function RequestCard({ request }: RequestCardProps) {
  return (
    <div className="border rounded-lg p-4 hover:shadow transition">
      <div className="flex justify-between items-start">
        <div>
          <h3 className="font-semibold text-lg">{request.title}</h3>
          <p className="text-gray-600 text-sm mt-1">{request.location.address}</p>
          <p className="text-gray-500 text-sm mt-2 line-clamp-2">{request.description}</p>
        </div>
        <RequestStatusBadge status={request.status} />
      </div>
      <div className="mt-4 flex justify-end">
        <Button variant="outline" asChild>
          <Link href={`/requests/${request.id}`}>Xem chi tiết</Link>
        </Button>
      </div>
    </div>
  );
}
