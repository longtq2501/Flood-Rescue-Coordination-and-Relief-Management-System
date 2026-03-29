import { MISSION_STATUS } from '../constants';

export type MissionStatus = keyof typeof MISSION_STATUS;

export interface Mission {
  id: string;
  title: string;
  description: string;
  location: {
    address: string;
    lat: number;
    lng: number;
  };
  peopleCount?: number;
  status: MissionStatus;
  createdAt: string;
  updatedAt: string;
  requestId: string;
  resultNote?: string;
}

export interface LocationUpdate {
  missionId: string;
  lat: number;
  lng: number;
  timestamp: string;
}