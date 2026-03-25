import { Injectable } from '@angular/core';

/** Shape of a game genre returned from GET /api/genres. */
export interface GameGenreResponse {
  id: number;
  name: string;
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  fontFamily: string;
  backgroundStyle: string;
  description: string;
}

/**
 * ThemeService applies a game-genre visual theme by injecting CSS custom
 * properties onto the document root element. All themed components consume
 * these variables (--primary, --secondary, --accent, --font-body) so colors
 * update instantly without a page reload.
 *
 * The `data-theme` attribute drives background-style variant rules defined
 * in `src/styles/theme.scss`.
 *
 * Call `applyGenre()` in every tournament detail component's `ngOnInit`,
 * and `resetTheme()` in `ngOnDestroy` to restore the default palette.
 */
@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  /**
   * Applies genre-specific CSS variables to :root so all themed
   * components update immediately without a page reload.
   *
   * @param genre - The game genre whose theme should be applied.
   */
  applyGenre(genre: GameGenreResponse): void {
    const root = document.documentElement;
    root.style.setProperty('--primary', genre.primaryColor);
    root.style.setProperty('--secondary', genre.secondaryColor);
    root.style.setProperty('--accent', genre.accentColor);
    root.style.setProperty('--font-body', genre.fontFamily);
    root.setAttribute('data-theme', genre.backgroundStyle.toLowerCase());
  }

  /**
   * Reverts all genre CSS variables to the application defaults.
   * Called when navigating away from a tournament detail page.
   */
  resetTheme(): void {
    const root = document.documentElement;
    root.style.removeProperty('--primary');
    root.style.removeProperty('--secondary');
    root.style.removeProperty('--accent');
    root.style.removeProperty('--font-body');
    root.removeAttribute('data-theme');
  }
}
