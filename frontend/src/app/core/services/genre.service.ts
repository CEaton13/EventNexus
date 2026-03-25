import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameGenreResponse } from './theme';

/** Request payload for creating or updating a genre. */
export interface GameGenreRequest {
  name: string;
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  backgroundStyle: string;
  fontFamily: string;
  iconPackKey: string;
  heroImageUrl?: string;
}

/**
 * GenreService communicates with the `/api/genres` endpoints.
 * GET endpoints are public. POST/PUT/DELETE require TOURNAMENT_ADMIN.
 */
@Injectable({ providedIn: 'root' })
export class GenreService {
  private readonly base = '/api/genres';

  constructor(private readonly http: HttpClient) {}

  /**
   * Returns all available game genres with their theme data.
   */
  getAll(): Observable<GameGenreResponse[]> {
    return this.http.get<GameGenreResponse[]>(this.base);
  }

  /**
   * Returns a single genre by ID.
   * @param id Genre primary key.
   */
  getById(id: number): Observable<GameGenreResponse> {
    return this.http.get<GameGenreResponse>(`${this.base}/${id}`);
  }

  /**
   * Creates a new genre. Requires TOURNAMENT_ADMIN.
   * @param data Genre creation payload.
   */
  create(data: GameGenreRequest): Observable<GameGenreResponse> {
    return this.http.post<GameGenreResponse>(this.base, data);
  }

  /**
   * Updates an existing genre. Requires TOURNAMENT_ADMIN.
   * @param id Genre primary key.
   * @param data Updated values.
   */
  update(id: number, data: GameGenreRequest): Observable<GameGenreResponse> {
    return this.http.put<GameGenreResponse>(`${this.base}/${id}`, data);
  }

  /**
   * Deletes a genre. Requires TOURNAMENT_ADMIN.
   * @param id Genre primary key.
   */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
