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
