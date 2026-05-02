export type Urgency = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RequestStatus = 'PENDING' | 'VERIFIED' | 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CONFIRMED' | 'CANCELLED';
export type TeamStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE';

export interface Request {
  id: number;
  citizenId: number;
  lat: number;
  lng: number;
  addressText?: string;
  description: string;
  numPeople: number;
  urgencyLevel: Urgency;
  status: RequestStatus;
  coordinatorId?: number;
  imageUrls: string[];
  statusHistories: StatusHistory[];
  verifiedAt?: string;
  completedAt?: string;
  confirmedAt?: string;
  createdAt: string;
}

export interface StatusHistory {
  fromStatus?: RequestStatus;
  toStatus: RequestStatus;
  changedBy: number;
  note?: string;
  changedAt: string;
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

export interface Vehicle {
  id: string;
  name: string;
  plate: string;
  status: 'AVAILABLE' | 'BUSY' | 'MAINTENANCE';
}

export interface Assignment {
  id: string;
  requestId: string;
  teamId: string;
  status: 'assigned' | 'en_route' | 'on_site' | 'completed';
  assignedAt: string;
  estimatedArrival: string;
}