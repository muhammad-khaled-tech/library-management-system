import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoanService } from './loan.service';

describe('LoanService', () => {
  let service: LoanService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LoanService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filters that are set', () => {
    service.search({ memberId: 3, status: 'OVERDUE' }, { page: 0, size: 20 }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/loans');
    expect(req.request.params.get('memberId')).toBe('3');
    expect(req.request.params.get('status')).toBe('OVERDUE');
    expect(req.request.params.has('bookId')).toBe(false);
    req.flush({ content: [], page: { size: 20, number: 0, totalElements: 0, totalPages: 0 } });
  });

  it('borrows a book', () => {
    service.borrow({ bookId: 10, memberId: 3 }).subscribe();

    const req = http.expectOne('/api/loans');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ bookId: 10, memberId: 3 });
    req.flush({});
  });

  it('returns a loan', () => {
    service.returnLoan(50).subscribe();

    expect(http.expectOne('/api/loans/50/return').request.method).toBe('POST');
  });
});
