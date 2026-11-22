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
import { BookDialog } from './book-dialog';
import { Book } from './book.model';
import { BookService } from './book.service';

@Component({
  selector: 'app-book-list',
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
  templateUrl: './book-list.html',
})
export class BookList {
  private readonly bookService = inject(BookService);
  private readonly dialog = inject(MatDialog);

  protected readonly columns = ['title', 'author', 'isbn', 'publishedYear', 'copies', 'actions'];
  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly list = new PagedList<Book>(
    (request) => this.bookService.search(this.search.value, request),
    { page: 0, size: 10, sort: 'title,asc' },
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

  edit(book: Book): void {
    this.openDialog(book);
  }

  remove(book: Book): void {
    this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, {
        data: { title: 'Delete book', message: `Delete "${book.title}"?` },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.bookService.delete(book.id)),
      )
      .subscribe(() => this.list.load());
  }

  private openDialog(book: Book | null): void {
    this.dialog
      .open(BookDialog, { data: book, width: '520px' })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => this.list.load());
  }
}
