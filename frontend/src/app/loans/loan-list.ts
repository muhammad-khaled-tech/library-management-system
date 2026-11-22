import { DecimalPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { filter, switchMap } from 'rxjs';
import { ConfirmDialog, ConfirmDialogData } from '../shared/confirm-dialog';
import { PagedList } from '../shared/paged-list';
import { BorrowDialog } from './borrow-dialog';
import { Loan, LoanStatus } from './loan.model';
import { LoanService } from './loan.service';

@Component({
  selector: 'app-loan-list',
  imports: [
    DecimalPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
  ],
  templateUrl: './loan-list.html',
})
export class LoanList {
  private readonly loanService = inject(LoanService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly columns = [
    'book',
    'member',
    'borrowedOn',
    'dueDate',
    'returnedOn',
    'lateFee',
    'status',
    'actions',
  ];
  /** '' means every status. */
  protected readonly status = new FormControl<LoanStatus | ''>('', { nonNullable: true });
  protected readonly list = new PagedList<Loan>(
    (request) => this.loanService.search({ status: this.status.value || undefined }, request),
    { page: 0, size: 10, sort: 'borrowedOn,desc' },
  );

  constructor() {
    this.status.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.list.firstPage());
    this.list.load();
  }

  borrow(): void {
    this.dialog
      .open(BorrowDialog, { width: '520px' })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe((loan) => {
        this.snackBar.open(`"${loan.bookTitle}" is due on ${loan.dueDate}`, 'Close', {
          duration: 5000,
        });
        this.list.load();
      });
  }

  returnLoan(loan: Loan): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: {
          title: 'Return book',
          message: `Return "${loan.bookTitle}" borrowed by ${loan.memberName}?`,
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.loanService.returnLoan(loan.id)),
      )
      .subscribe((returned) => {
        const fee = returned.lateFee ?? 0;
        const message =
          fee > 0 ? `Returned late. Late fee: ${fee.toFixed(2)}` : 'Returned on time, no fee';
        this.snackBar.open(message, 'Close', { duration: 5000 });
        this.list.load();
      });
  }
}
