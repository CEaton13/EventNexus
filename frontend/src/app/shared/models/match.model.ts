export type MatchStatus = 'UNSCHEDULED' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'BYE';

export interface TeamSlot {
  id: number;
  name: string;
  tag: string;
}

export interface MatchResponse {
  id: number;
  tournamentId: number;
  roundNumber: number;
  matchNumber: number;
  teamA: TeamSlot | null;
  teamB: TeamSlot | null;
  winner: TeamSlot | null;
  status: MatchStatus;
  scheduledTime: string | null;
  nextMatchId: number | null;
}

export interface BracketRound {
  roundNumber: number;
  roundLabel: string;
  matches: MatchResponse[];
}

export interface BracketResponse {
  tournamentId: number;
  tournamentName: string;
  totalRounds: number;
  bracketSize: number;
  rounds: BracketRound[];
}

export interface ConflictCheckResponse {
  hasTeamConflict: boolean;
  hasVenueConflict: boolean;
  teamConflictDetails?: string;
  venueConflictDetails?: string;
}

export interface MatchDetail {
  id: number;
  tournamentId: number;
  roundNumber: number;
  matchNumber: number;
  teamA: { id: number; name: string; tag: string } | null;
  teamB: { id: number; name: string; tag: string } | null;
  winnerId: number | null;
  status: MatchStatus;
  scheduledTime: string | null;
  venueId: number | null;
  venueName: string | null;
  stationNumber: number | null;
}
