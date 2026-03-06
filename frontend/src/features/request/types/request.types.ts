export type UrgencyLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RequestStatus =
    | 'PENDING' | 'VERIFIED' | 'ASSIGNED'
    | 'IN_PROGRESS' | 'COMPLETED' | 'CONFIRMED' | 'CANCELLED';

export interface RescueRequestResponse {
    id: number;
    citizenId: number;
    lat: number;
    lng: number;
    addressText?: string;
    description: string;
    numPeople: number;
    urgencyLevel: UrgencyLevel;
    status: RequestStatus;
    coordinatorId?: number;
    imageUrls: string[];
    statusHistories: StatusHistoryResponse[];
    verifiedAt?: string;
    completedAt?: string;
    confirmedAt?: string;
    createdAt: string;
}

export interface StatusHistoryResponse {
    fromStatus?: RequestStatus;
    toStatus: RequestStatus;
    changedBy: number;
    note?: string;
    changedAt: string;
}

export interface CreateRescueRequestDto {
    lat: number;
    lng: number;
    addressText?: string;
    description: string;
    numPeople?: number;
}

export interface VerifyRequestDto {
    urgencyLevel?: UrgencyLevel;
    note?: string;
}

export interface CancelRequestDto {
    reason: string;
}

export interface RequestFilterParams {
    status?: RequestStatus;
    urgencyLevel?: UrgencyLevel;
    fromDate?: string;
    toDate?: string;
}