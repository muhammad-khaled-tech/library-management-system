import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './error.interceptor';

describe('errorMessage', () => {
  it('explains a network failure', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Cannot reach the server');
  });

  it('uses the problem detail from the API', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        title: 'Conflict',
        status: 409,
        detail: 'Author 1 still has books and cannot be deleted',
      },
    });

    expect(errorMessage(error)).toBe('Author 1 still has books and cannot be deleted');
  });

  it('lists field validation errors', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        detail: 'Invalid request content.',
        errors: { isbn: 'invalid ISBN', totalCopies: 'must be greater than 0' },
      },
    });

    expect(errorMessage(error)).toBe('isbn: invalid ISBN, totalCopies: must be greater than 0');
  });
});
