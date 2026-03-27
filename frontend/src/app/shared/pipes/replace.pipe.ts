import { Pipe, PipeTransform } from '@angular/core';

/** Replaces all occurrences of a substring within a string. */
@Pipe({ name: 'replace', standalone: false })
export class ReplacePipe implements PipeTransform {
  transform(value: string, search: string, replacement: string): string {
    if (!value) return value;
    return value.split(search).join(replacement);
  }
}
