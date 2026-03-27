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
  iconPackKey: string;
  heroImageUrl?: string;
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
 * Also dynamically loads the genre's Google Font via a `<link>` tag so the
 * font renders correctly when `fontFamily` references a Google Fonts name.
 *
 * Call `applyGenre()` in every tournament detail component's `ngOnInit`,
 * and `resetTheme()` in `ngOnDestroy` to restore the default palette.
 */
@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly loadedFonts = new Set<string>();

  /**
   * Applies genre-specific CSS variables to :root so all themed
   * components update immediately without a page reload.
   * Also loads the genre's Google Font if not already present.
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
    this.loadGoogleFont(genre.fontFamily);
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

  /**
   * Dynamically injects a Google Fonts `<link>` tag for the given font-family
   * CSS value (e.g. "Rajdhani, sans-serif" → loads "Rajdhani").
   * Skips loading if the font was already injected in this session.
   *
   * @param fontFamily - CSS font-family string from the genre's theme data.
   */
  private loadGoogleFont(fontFamily: string): void {
    const primaryFont = fontFamily.split(',')[0].trim().replace(/['"]/g, '');
    if (!primaryFont || this.loadedFonts.has(primaryFont)) {
      return;
    }
    const linkId = `gfont-${primaryFont.replace(/\s+/g, '-').toLowerCase()}`;
    if (document.getElementById(linkId)) {
      this.loadedFonts.add(primaryFont);
      return;
    }
    const link = document.createElement('link');
    link.id = linkId;
    link.rel = 'stylesheet';
    link.href = `https://fonts.googleapis.com/css2?family=${encodeURIComponent(primaryFont)}:wght@400;600;700&display=swap`;
    document.head.appendChild(link);
    this.loadedFonts.add(primaryFont);
  }
}
