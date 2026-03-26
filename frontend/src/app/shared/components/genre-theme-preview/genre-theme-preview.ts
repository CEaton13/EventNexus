import { Component, Input } from '@angular/core';
import { GameGenreResponse } from '../../../core/services/theme';

/**
 * GenreThemePreviewComponent renders a scoped, inline-styled preview card for
 * a selected game genre. Unlike `GenrePreview` (which is used in grid
 * selection), this component is a larger detailed preview shown alongside the
 * genre grid once a genre is chosen.
 *
 * It does NOT call `ThemeService.applyGenre()` — all styles are applied via
 * inline `[ngStyle]` so the global CSS variables are not affected.
 *
 * @example
 * ```html
 * <app-genre-theme-preview [genre]="selectedGenre()"></app-genre-theme-preview>
 * ```
 */
@Component({
  selector: 'app-genre-theme-preview',
  standalone: false,
  templateUrl: './genre-theme-preview.html',
  styleUrl: './genre-theme-preview.scss',
})
export class GenreThemePreview {
  /** The genre to preview. If null, nothing is rendered. */
  @Input() genre: GameGenreResponse | null = null;

  /**
   * Builds the container style using the genre's primary and secondary colors
   * as background and text, with the font-family applied.
   */
  previewStyles(): Record<string, string> {
    if (!this.genre) return {};
    return {
      'background-color': this.genre.primaryColor,
      'color': this.genre.secondaryColor,
      'font-family': this.genre.fontFamily,
      'border': `2px solid ${this.genre.accentColor}`,
    };
  }

  /**
   * Builds the badge style using the accent color as its background.
   */
  badgeStyles(): Record<string, string> {
    if (!this.genre) return {};
    return {
      'background-color': this.genre.accentColor,
      'color': this.genre.primaryColor,
    };
  }

  /**
   * Builds the font sample style to demonstrate the genre's font-family.
   */
  fontStyles(): Record<string, string> {
    if (!this.genre) return {};
    return {
      'font-family': this.genre.fontFamily,
      'color': this.genre.secondaryColor,
      'opacity': '0.8',
    };
  }
}
