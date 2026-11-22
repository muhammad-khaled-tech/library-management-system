import { AsyncPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize, map } from 'rxjs';
import { Author } from '../authors/author.model';
import { AuthorService } from '../authors/author.service';
import { optionSelected, searchAsYouType } from '../shared/autocomplete';
import { Book, BookRequest } from './book.model';
import { BookService } from './book.service';
import { isbnValidator } from './isbn.validator';

/** Creates a book, or edits the one passed as dialog data. Stays open if the save fails. */
@Component({
  selector: 'app-book-dialog',
  imports: [
    AsyncPipe,
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './book-dialog.html',
})
export class BookDialog {
  private readonly bookService = inject(BookService);
  private readonly authorService = inject(AuthorService);
  private readonly dialogRef = inject<MatDialogRef<BookDialog, Book>>(MatDialogRef);
  protected readonly book = inject<Book | null>(MAT_DIALOG_DATA);
  protected readonly saving = signal(false);
  protected readonly currentYear = new Date().getFullYear();

  protected readonly form = new FormGroup({
    isbn: new FormControl(this.book?.isbn ?? '', {
      nonNullable: true,
      validators: [Validators.required, isbnValidator],
    }),
    title: new FormControl(this.book?.title ?? '', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)],
    }),
    publishedYear: new FormControl<number | null>(this.book?.publishedYear ?? null, [
      Validators.min(1),
      Validators.max(this.currentYear),
    ]),
    totalCopies: new FormControl(this.book?.totalCopies ?? 1, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(1)],
    }),
    author: new FormControl<Author | string | null>(
      this.book ? { id: this.book.authorId, name: this.book.authorName } : null,
      [Validators.required, optionSelected],
    ),
  });

  protected readonly authors$ = searchAsYouType(this.form.controls.author, (name) =>
    this.authorService
      .search(name, { page: 0, size: 10, sort: 'name,asc' })
      .pipe(map((page) => page.content)),
  );

  protected authorName(author: Author | null): string {
    return author?.name ?? '';
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { isbn, title, publishedYear, totalCopies, author } = this.form.getRawValue();
    const request: BookRequest = {
      isbn,
      title,
      publishedYear,
      totalCopies,
      authorId: (author as Author).id,
    };
    const call = this.book
      ? this.bookService.update(this.book.id, request)
      : this.bookService.create(request);

    this.saving.set(true);
    call.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: (saved) => this.dialogRef.close(saved),
    });
  }
}
