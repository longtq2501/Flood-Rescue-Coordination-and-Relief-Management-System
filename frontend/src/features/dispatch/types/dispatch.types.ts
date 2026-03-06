export type TeamStatus = 'AVAILABLE' | 'BUSY' | 'RETURNING' | 'OFFLINE';
export type AssignmentStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface RescueTeamResponse {
    id: number;
    name: string;
    leaderId: number;
    capacity: number;
    status: TeamStatus;
    currentLat?: number;
    currentLng?: number;
    createdAt: string;
    members: TeamMemberResponse[];
}

export interface TeamMemberResponse {
    userId: number;
    joinedAt: string;
}

export interface AssignmentResponse {
    id: number;
    requestId: number;
    teamId: number;
    teamName: string;
    vehicleId: number;
    coordinatorId: number;
    status: AssignmentStatus;
    assignedAt: string;
    startedAt?: string;
    completedAt?: string;
    resultNote?: string;
}

export interface AssignTeamRequest {
    requestId: number;
    teamId: number;
    vehicleId: number;
}

export interface LocationUpdateRequest {
    lat: number;
    lng: number;
    speed?: number;
    heading?: number;
}

export interface MapDataResponse {
    teams: TeamLocationDto[];
}

export interface TeamLocationDto {
    teamId: number;
    teamName: string;
    status: TeamStatus;
    lat?: number;
    lng?: number;
    lastUpdated?: string;
}