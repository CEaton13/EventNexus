import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Factory that returns a cross-field ValidatorFn ensuring the date value of
 * `endField` is strictly after the date value of `startField` within a
 * FormGroup.
 *
 * Apply to a FormGroup (not a single control):
 * ```typescript
 * this.fb.group({ ... }, { validators: dateRangeValidator('start', 'end') })
 * // or
 * group.addValidators(dateRangeValidator('start', 'end'));
 * ```
 *
 * @param startField - Name of the control holding the earlier date.
 * @param endField   - Name of the control that must be after `startField`.
 * @param message    - Optional human-readable error message. Defaults to
 *                     `"<endField> must be after <startField>"`.
 * @returns A `ValidatorFn` that sets `{ dateRange: { message } }` on the
 *          group when the constraint is violated, or `null` when valid.
 */
export function dateRangeValidator(
  startField: string,
  endField: string,
  message?: string,
): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const startControl = group.get(startField);
    const endControl = group.get(endField);

    if (!startControl || !endControl) {
      return null;
    }

    const startValue = startControl.value;
    const endValue = endControl.value;

    if (!startValue || !endValue) {
      return null;
    }

    const startDate = new Date(startValue);
    const endDate = new Date(endValue);

    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
      return null;
    }

    if (endDate <= startDate) {
      return {
        dateRange: {
          message: message ?? `${endField} must be after ${startField}`,
        },
      };
    }

    return null;
  };
}
