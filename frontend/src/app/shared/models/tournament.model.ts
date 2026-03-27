import { GameGenreResponse } from '../../core/services/theme';
import { VenueResponse } from './venue.model';

export type TournamentStatus =
  | 'DRAFT'
  | 'REGISTRATION_OPEN'
  | 'REGISTRATION_CLOSED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'ARCHIVED';

export type TournamentFormat = 'SINGLE_ELIMINATION' | 'DOUBLE_ELIMINATION' | 'ROUND_ROBIN';

export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

/** Lightweight card shape returned by GET /tournaments (list). */
export interface TournamentSummary {
  id: number;
  name: string;
  gameTitle: string;
  status: TournamentStatus;
  format: TournamentFormat;
  maxTeams: number;
  startDate: string;
  endDate: string;
  gameGenreId: number;
  gameGenreName: string;
}

/** Full detail shape returned by GET /tournaments/:id. */
export interface TournamentDetail {
  id: number;
  name: string;
  description: string;
  gameTitle: string;
  status: TournamentStatus;
  format: TournamentFormat;
  maxTeams: number;
  registrationStart: string;
  registrationEnd: string;
  startDate: string;
  endDate: string;
  gameGenre: GameGenreResponse;
  venue: VenueResponse | null;
  createdByUsername: string;
  createdAt: string;
  updatedAt: string;
}

/** Paginated wrapper returned by the backend PageResponse<T>. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface RegistrationResponse {
  id: number;
  tournamentId: number;
  tournamentName: string;
  teamId: number;
  teamName: string;
  seed: number | null;
  registrationStatus: RegistrationStatus;
  registeredAt: string;
}

export interface StandingsResponse {
  teamId: number;
  teamName: string;
  teamTag: string;
  logoUrl: string | null;
  wins: number;
  losses: number;
  points: number;
  rank: number;
}

/** Aggregated admin metrics returned by GET /admin/dashboard. */
export interface DashboardSummary {
  tournamentsByStatus: Record<string, number>;
  totalTeams: number;
  activePlayers: number;
  upcomingMatchesNext7Days: number;
}

export interface TournamentCreateRequest {
  name: string;
  description?: string;
  gameTitle?: string;
  format: TournamentFormat;
  maxTeams: number;
  registrationStart: string;
  registrationEnd: string;
  startDate: string;
  endDate: string;
  venueId: number;
  gameGenreId: number;
}
