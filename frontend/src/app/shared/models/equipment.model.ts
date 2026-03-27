export interface EquipmentResponse {
  id: number;
  venueId: number;
  venueName: string;
  name: string;
  category: string;
  serialNumber: string | null;
  isAvailable: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EquipmentRequest {
  venueId: number;
  name: string;
  category: string;
  serialNumber?: string;
}

export interface LoadoutResponse {
  id: number;
  equipmentId: number;
  equipmentName: string;
  equipmentCategory: string;
  teamId: number;
  teamName: string;
  tournamentId: number;
  assignedAt: string;
  returnedAt: string | null;
}

export interface LoadoutRequest {
  equipmentId: number;
  teamId: number;
}
