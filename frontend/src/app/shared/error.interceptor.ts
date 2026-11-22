import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/** RFC 9457 body that the backend's GlobalExceptionHandler returns. */
interface ProblemDetail {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}

export function errorMessage(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running?';
  }
  const problem = error.error as ProblemDetail | null;
  if (problem?.errors) {
    return Object.entries(problem.errors)
      .map(([field, message]) => `${field}: ${message}`)
      .join(', ');
  }
  return problem?.detail ?? `Request failed (${error.status})`;
}

/** Shows every failed API call in one place, then lets the caller react to the error too. */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const snackBar = inject(MatSnackBar);
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      snackBar.open(errorMessage(error), 'Close', { duration: 6000 });
      return throwError(() => error);
    }),
  );
};
