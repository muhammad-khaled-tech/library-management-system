import { Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, filter, switchMap } from 'rxjs';
import { ConfirmDialog, ConfirmDialogData } from '../shared/confirm-dialog';
import { PagedList } from '../shared/paged-list';
import { MemberDialog } from './member-dialog';
import { Member } from './member.model';
import { MemberService } from './member.service';

@Component({
  selector: 'app-member-list',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,
  ],
  templateUrl: './member-list.html',
})
export class MemberList {
  private readonly memberService = inject(MemberService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['fullName', 'email', 'actions'];
  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly list = new PagedList<Member>(
    (request) => this.memberService.search(this.search.value, request),
    { page: 0, size: 10, sort: 'fullName,asc' },
  );

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.list.firstPage());
    this.list.load();
  }

  add(): void {
    this.openDialog(null);
  }

  edit(member: Member): void {
    this.openDialog(member);
  }

  remove(member: Member): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: { title: 'Delete member', message: `Delete "${member.fullName}"?` },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.memberService.delete(member.id)),
      )
      .subscribe(() => this.list.load());
  }

  private openDialog(member: Member | null): void {
    this.dialog
      .open(MemberDialog, { data: member, width: '460px' })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => this.list.load());
  }
}
