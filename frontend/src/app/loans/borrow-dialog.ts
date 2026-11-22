import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize, map } from 'rxjs';
import { Book } from '../books/book.model';
import { BookService } from '../books/book.service';
import { Member } from '../members/member.model';
import { MemberService } from '../members/member.service';
import { optionSelected, searchAsYouType } from '../shared/autocomplete';
import { Loan } from './loan.model';
import { LoanService } from './loan.service';

/**
 * Lends a book to a member. The API enforces the rules (copies left, 3-loan limit);
 * if it refuses, the dialog stays open and the error interceptor shows why.
 */
@Component({
  selector: 'app-borrow-dialog',
  imports: [
    AsyncPipe,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './borrow-dialog.html',
})
export class BorrowDialog {
  private readonly loanService = inject(LoanService);
  private readonly bookService = inject(BookService);
  private readonly memberService = inject(MemberService);
  private readonly dialogRef = inject<MatDialogRef<BorrowDialog, Loan>>(MatDialogRef);
  protected readonly saving = signal(false);

  protected readonly form = new FormGroup({
    book: new FormControl<Book | string | null>(null, [Validators.required, optionSelected]),
    member: new FormControl<Member | string | null>(null, [Validators.required, optionSelected]),
  });

  protected readonly books$ = searchAsYouType(this.form.controls.book, (text) =>
    this.bookService
      .search(text, { page: 0, size: 10, sort: 'title,asc' })
      .pipe(map((page) => page.content)),
  );

  protected readonly members$ = searchAsYouType(this.form.controls.member, (text) =>
    this.memberService
      .search(text, { page: 0, size: 10, sort: 'fullName,asc' })
      .pipe(map((page) => page.content)),
  );

  protected bookTitle(book: Book | null): string {
    return book?.title ?? '';
  }

  protected memberName(member: Member | null): string {
    return member?.fullName ?? '';
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const book = this.form.controls.book.value as Book;
    const member = this.form.controls.member.value as Member;

    this.saving.set(true);
    this.loanService
      .borrow({ bookId: book.id, memberId: member.id })
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({ next: (loan) => this.dialogRef.close(loan) });
  }
}
