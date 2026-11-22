import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { LoanList } from './loan-list';
import { Loan } from './loan.model';
import { LoanService } from './loan.service';

describe('LoanList', () => {
  const overdue: Loan = {
    id: 50,
    bookId: 10,
    bookTitle: 'Palace Walk',
    memberId: 3,
    memberName: 'Sara Ali',
    borrowedOn: '2026-03-01',
    dueDate: '2026-03-15',
    returnedOn: null,
    lateFee: null,
    status: 'OVERDUE',
  };
  const returned: Loan = {
    ...overdue,
    id: 51,
    returnedOn: '2026-03-18',
    lateFee: 15,
    status: 'RETURNED',
  };
  const loanService = {
    search: vi.fn(() =>
      of({
        content: [overdue, returned],
        page: { size: 10, number: 0, totalElements: 2, totalPages: 1 },
      }),
    ),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [LoanList],
      providers: [{ provide: LoanService, useValue: loanService }],
    });
  });

  it('lists newest loans first and offers Return only for open loans', async () => {
    const fixture = TestBed.createComponent(LoanList);
    await fixture.whenStable();

    expect(loanService.search).toHaveBeenCalledWith(
      { status: undefined },
      { page: 0, size: 10, sort: 'borrowedOn,desc' },
    );
    const rows = (fixture.nativeElement as HTMLElement).querySelectorAll('tr.mat-mdc-row');
    expect(rows[0].querySelector('.status.overdue')?.textContent).toBe('OVERDUE');
    expect(rows[0].textContent).toContain('Return');
    expect(rows[1].textContent).toContain('15.00');
    expect(rows[1].querySelector('button')).toBeNull();
  });

  it('filters by the chosen status from the first page', async () => {
    const fixture = TestBed.createComponent(LoanList);
    await fixture.whenStable();

    fixture.componentInstance['status'].setValue('OVERDUE');

    expect(loanService.search).toHaveBeenLastCalledWith(
      { status: 'OVERDUE' },
      { page: 0, size: 10, sort: 'borrowedOn,desc' },
    );
  });
});
