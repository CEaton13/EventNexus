import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameGenreResponse } from './theme';

/**
 * GenreService communicates with the `/api/genres` endpoints.
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
}
