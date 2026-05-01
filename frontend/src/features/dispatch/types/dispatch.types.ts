export type Team = {
  id: number;
  name: string;
  status: "AVAILABLE" | "BUSY" | "RETURNING" | "OFFLINE";
  capacity: number;
  memberCount: number;
};

export type Assignment = {
  id: number;
  requestId: number;
  teamId: number;
  teamName: string;
  vehicleId: number;
  vehiclePlate: string;
  status: "ACTIVE" | "COMPLETED" | "CANCELLED";
  assignedAt?: string;
  startedAt?: string;
  completedAt?: string;
};

export type DispatchAssignmentPayload = {
  requestId: number;
  teamId: number;
  vehicleId: number;
  note?: string;
};
