export type UrgencyLevel = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

export type RequestStatus =
  | "PENDING"
  | "VERIFIED"
  | "ASSIGNED"
  | "COMPLETED"
  | "CONFIRMED"
  | "CANCELLED";

export type RescueRequestSummary = {
  id: number;
  lat: number;
  lng: number;
  addressText?: string | null;
  description: string;
  numPeople: number;
  urgencyLevel: UrgencyLevel;
  status: RequestStatus;
  createdAt: string;
  citizenName?: string | null;
};

export type RescueRequestDetail = RescueRequestSummary & {
  citizenId: number;
  citizenName?: string;
  citizenPhone?: string;
  imageUrls?: string[];
  statusHistory?: Array<{
    fromStatus: RequestStatus | null;
    toStatus: RequestStatus;
    changedBy: string;
    changedAt: string;
  }>;
};

export type PageResult<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last?: boolean;
};

export type CreateRescueRequestPayload = {
  lat?: number;
  lng?: number;
  addressText?: string;
  description: string;
  numPeople: number;
  urgencyLevel: UrgencyLevel;
  images?: File[] | null;
};

export interface RequestFilters {
  status?: RequestStatus | "";
  urgencyLevel?: UrgencyLevel | "";
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
  search?: string;
}
