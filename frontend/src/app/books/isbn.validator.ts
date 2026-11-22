import { AbstractControl, ValidationErrors } from '@angular/forms';

/** True for a valid ISBN-13; hyphens and spaces are ignored, like the backend's @ISBN check. */
export function isValidIsbn13(value: string): boolean {
  const digits = value.replace(/[\s-]/g, '');
  if (!/^\d{13}$/.test(digits)) {
    return false;
  }
  const sum = [...digits.slice(0, 12)].reduce(
    (total, digit, index) => total + Number(digit) * (index % 2 === 0 ? 1 : 3),
    0,
  );
  return (10 - (sum % 10)) % 10 === Number(digits[12]);
}

export function isbnValidator(control: AbstractControl<string>): ValidationErrors | null {
  return !control.value || isValidIsbn13(control.value) ? null : { isbn: true };
}
