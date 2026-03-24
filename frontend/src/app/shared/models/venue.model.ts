export interface VenueResponse {
  id: number;
  name: string;
  location: string;
  stationCount: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  activeMatches?: number;
  availableStations?: number;
  utilizationPct?: number;
}
