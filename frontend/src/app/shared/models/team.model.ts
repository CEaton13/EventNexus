export interface TeamResponse {
  id: number;
  name: string;
  tag: string;
  logoUrl: string | null;
  homeRegion: string | null;
  managerUsername: string;
  playerCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TeamRequest {
  name: string;
  tag: string;
  logoUrl?: string;
  homeRegion?: string;
}
