export type Urgency = 'critical' | 'high' | 'medium' | 'low';
export type RequestStatus = 'pending' | 'assigned' | 'in_progress' | 'completed' | 'cancelled';
export type TeamStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE';

export interface Request {
  id: string;
  title: string;
  description: string;
  location: string;
  urgency: Urgency;
  status: RequestStatus;
  createdAt: string;
  customerName: string;
  customerPhone: string;
}

export interface Team {
  id: string;
  name: string;
  status: TeamStatus;
  location: {
    lat: number;
    lng: number;
  };
  members: number;
  vehicle: string;
  currentAssignment?: string;
}

export interface Assignment {
  id: string;
  requestId: string;
  teamId: string;
  status: 'assigned' | 'en_route' | 'on_site' | 'completed';
  assignedAt: string;
  estimatedArrival: string;
}