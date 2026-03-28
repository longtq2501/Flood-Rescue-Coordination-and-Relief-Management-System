import { REQUEST_STATUS, URGENCY_LEVELS } from '../constants';

export type RequestStatus = keyof typeof REQUEST_STATUS;
export type UrgencyLevel = keyof typeof URGENCY_LEVELS;

export interface CitizenRequest {
  id: string;
  title: string;
  description: string;
  location: {
    address: string;
    lat: number;
    lng: number;
  };
  urgency: UrgencyLevel;
  status: RequestStatus;
  images: string[]; // URLs of uploaded images
  peopleCount?: number;
  createdAt: string;
  updatedAt: string;
  assignedTeam?: {
    id: string;
    name: string;
    eta?: string;
  };
}

export interface TimelineEvent {
  status: RequestStatus;
  timestamp: string;
  note?: string;
}

export interface CreateRequestPayload {
  title: string;
  description: string;
  location: { lat: number; lng: number; address: string };
  urgency: UrgencyLevel;
  images: File[];
  peopleCount?: number;
}

export interface UpdateRequestPayload {
  title?: string;
  description?: string;
  location?: { lat: number; lng: number; address: string };
  urgency?: UrgencyLevel;
  peopleCount?: number;
}