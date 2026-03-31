export interface MyRegistrationResponse {
  tournamentId: number;
  tournamentName: string;
  gameTitle: string;
  tournamentStatus: string;
  startDate: string | null;
  teamId: number;
  teamName: string;
  teamTag: string;
  registrationStatus: string;
  registeredAt: string;
}
