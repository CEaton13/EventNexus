export interface PlayerResponse {
  id: number;
  teamId: number;
  teamName: string;
  gamerTag: string;
  realName: string | null;
  position: string | null;
  country: string | null;
  avatarUrl: string | null;
  active: boolean;
  userId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface PlayerStatsResponse {
  tournamentId: number;
  tournamentName: string;
  wins: number;
  losses: number;
  mvpCount: number;
}

export interface PlayerRequest {
  gamerTag: string;
  realName?: string;
  position?: string;
  country?: string;
  avatarUrl?: string;
}
