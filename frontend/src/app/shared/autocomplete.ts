import { AbstractControl, FormControl, ValidationErrors } from '@angular/forms';
import {
  Observable,
  catchError,
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
  of,
  startWith,
  switchMap,
} from 'rxjs';

/**
 * Options for a mat-autocomplete that searches the API as the user types.
 * The control holds the typed text while searching and the picked object once an option is chosen.
 */
export function searchAsYouType<T extends object>(
  control: FormControl<T | string | null>,
  search: (text: string) => Observable<T[]>,
): Observable<T[]> {
  return control.valueChanges.pipe(
    startWith(control.value),
    filter((value): value is string | null => typeof value !== 'object' || value === null),
    map((text) => text ?? ''),
    debounceTime(250),
    distinctUntilChanged(),
    switchMap((text) => search(text).pipe(catchError(() => of([])))),
  );
}

/** Fails until an option has been picked, so free text never reaches the API. */
export function optionSelected(control: AbstractControl): ValidationErrors | null {
  return control.value !== null && typeof control.value === 'object'
    ? null
    : { optionSelected: true };
}
