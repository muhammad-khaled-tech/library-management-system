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
import { AuthorDialog } from './author-dialog';
import { Author } from './author.model';
import { AuthorService } from './author.service';

@Component({
  selector: 'app-author-list',
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
  templateUrl: './author-list.html',
})
export class AuthorList {
  private readonly authorService = inject(AuthorService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['name', 'actions'];
  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly list = new PagedList<Author>(
    (request) => this.authorService.search(this.search.value, request),
    { page: 0, size: 10, sort: 'name,asc' },
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

  edit(author: Author): void {
    this.openDialog(author);
  }

  remove(author: Author): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: { title: 'Delete author', message: `Delete "${author.name}"?` },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.authorService.delete(author.id)),
      )
      .subscribe(() => this.list.load());
  }

  private openDialog(author: Author | null): void {
    this.dialog
      .open(AuthorDialog, { data: author, width: '420px' })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => this.list.load());
  }
}
