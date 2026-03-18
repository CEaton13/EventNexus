import { Component, Input, Output, EventEmitter } from '@angular/core';
import { GameGenreResponse } from '../../../core/services/theme';

/**
 * GenrePreviewComponent renders a themed preview card for a single game genre.
 *
 * Used in the tournament creation wizard's genre-selection step so admins can
 * see a live preview of each genre's color palette before choosing one.
 *
 * @example
 * ```html
 * <app-genre-preview
 *   [genre]="genre"
 *   [selected]="selectedGenreId === genre.id"
 *   (genreSelected)="onGenreSelect($event)">
 * </app-genre-preview>
 * ```
 */
@Component({
  selector: 'app-genre-preview',
  standalone: false,
  templateUrl: './genre-preview.html',
  styleUrl: './genre-preview.scss',
})
export class GenrePreview {
  /** The genre to display. */
  @Input({ required: true }) genre!: GameGenreResponse;

  /** Whether this card is currently selected in the wizard. */
  @Input() selected = false;

  /** Emits the genre when the card is clicked. */
  @Output() genreSelected = new EventEmitter<GameGenreResponse>();

  /** Builds an inline-style object from the genre's palette for the card header. */
  get headerStyle(): Record<string, string> {
    return {
      'background-color': this.genre.primaryColor,
      'border-bottom': `3px solid ${this.genre.accentColor}`,
      'font-family': this.genre.fontFamily,
    };
  }

  /** Builds an inline-style object for the card body accent strip. */
  get accentStyle(): Record<string, string> {
    return { 'background-color': this.genre.accentColor };
  }

  select(): void {
    this.genreSelected.emit(this.genre);
  }
}
