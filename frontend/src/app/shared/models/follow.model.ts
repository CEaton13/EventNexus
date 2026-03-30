export interface TeamFollowResponse {
  id: number;
  teamId: number;
  teamName: string;
  teamTag: string;
  logoUrl: string | null;
  createdAt: string;
}

export interface TournamentFavoriteResponse {
  id: number;
  tournamentId: number;
  tournamentName: string;
  gameTitle: string;
  status: string;
  startDate: string | null;
  createdAt: string;
}

export interface FollowStatus {
  following: boolean;
}

export interface FavoriteStatus {
  favorited: boolean;
}
