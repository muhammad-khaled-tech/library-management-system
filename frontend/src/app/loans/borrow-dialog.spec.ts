import { TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { BookService } from '../books/book.service';
import { MemberService } from '../members/member.service';
import { BorrowDialog } from './borrow-dialog';
import { LoanService } from './loan.service';

describe('BorrowDialog', () => {
  const emptyPage = of({ content: [], page: {} });
  const loanService = { borrow: vi.fn() };
  const dialogRef = { close: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      imports: [BorrowDialog],
      providers: [
        { provide: LoanService, useValue: loanService },
        { provide: BookService, useValue: { search: () => emptyPage } },
        { provide: MemberService, useValue: { search: () => emptyPage } },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    });
  });

  it('borrows the picked book for the picked member', () => {
    loanService.borrow.mockReturnValue(of({ id: 50 }));
    const dialog = TestBed.createComponent(BorrowDialog).componentInstance;
    dialog['form'].setValue({
      book: {
        id: 10,
        isbn: '9780306406157',
        title: 'Palace Walk',
        publishedYear: 1956,
        totalCopies: 2,
        availableCopies: 1,
        authorId: 1,
        authorName: 'Naguib Mahfouz',
      },
      member: { id: 3, fullName: 'Sara Ali', email: 'sara@example.com' },
    });

    dialog.save();

    expect(loanService.borrow).toHaveBeenCalledWith({ bookId: 10, memberId: 3 });
    expect(dialogRef.close).toHaveBeenCalledWith({ id: 50 });
  });

  it('needs both a book and a member picked from the lists', () => {
    const dialog = TestBed.createComponent(BorrowDialog).componentInstance;
    dialog['form'].setValue({ book: 'Palace', member: null });

    dialog.save();

    expect(loanService.borrow).not.toHaveBeenCalled();
  });
});
