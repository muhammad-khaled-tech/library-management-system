import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { AuthorService } from '../authors/author.service';
import { BookDialog } from './book-dialog';
import { Book } from './book.model';
import { BookService } from './book.service';

describe('BookDialog', () => {
  const bookService = { create: vi.fn(), update: vi.fn() };
  const authorService = { search: vi.fn(() => of({ content: [], page: {} })) };
  const dialogRef = { close: vi.fn() };

  function setUp(data: Book | null) {
    TestBed.configureTestingModule({
      imports: [BookDialog],
      providers: [
        { provide: BookService, useValue: bookService },
        { provide: AuthorService, useValue: authorService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    return TestBed.createComponent(BookDialog).componentInstance;
  }

  beforeEach(() => vi.clearAllMocks());

  it('sends the picked author as authorId', () => {
    bookService.create.mockReturnValue(of({ id: 10 }));
    const dialog = setUp(null);
    dialog['form'].setValue({
      isbn: '978-0-306-40615-7',
      title: 'Palace Walk',
      publishedYear: 1956,
      totalCopies: 3,
      author: { id: 1, name: 'Naguib Mahfouz' },
    });

    dialog.save();

    expect(bookService.create).toHaveBeenCalledWith({
      isbn: '978-0-306-40615-7',
      title: 'Palace Walk',
      publishedYear: 1956,
      totalCopies: 3,
      authorId: 1,
    });
    expect(dialogRef.close).toHaveBeenCalledWith({ id: 10 });
  });

  it('blocks a bad ISBN check digit and an author typed but not picked', () => {
    const dialog = setUp(null);
    dialog['form'].setValue({
      isbn: '9780306406158',
      title: 'Palace Walk',
      publishedYear: null,
      totalCopies: 1,
      author: 'Naguib',
    });

    dialog.save();

    expect(dialog['form'].controls.isbn.hasError('isbn')).toBe(true);
    expect(dialog['form'].controls.author.hasError('optionSelected')).toBe(true);
    expect(bookService.create).not.toHaveBeenCalled();
  });

  it('searches authors with the text typed into the author field', async () => {
    vi.useFakeTimers();
    const fixture = TestBed.configureTestingModule({
      imports: [BookDialog],
      providers: [
        { provide: BookService, useValue: bookService },
        { provide: AuthorService, useValue: authorService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: null },
      ],
    }).createComponent(BookDialog);
    fixture.detectChanges();
    const input = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(
      'input[formcontrolname="author"]',
    )!;

    input.focus();
    input.value = 'tah';
    input.dispatchEvent(new Event('input'));
    await vi.advanceTimersByTimeAsync(300);

    expect(authorService.search).toHaveBeenLastCalledWith('tah', {
      page: 0,
      size: 10,
      sort: 'name,asc',
    });
    vi.useRealTimers();
  });

  it('starts from the book being edited', () => {
    const dialog = setUp({
      id: 10,
      isbn: '9780306406157',
      title: 'Palace Walk',
      publishedYear: 1956,
      totalCopies: 3,
      availableCopies: 1,
      authorId: 1,
      authorName: 'Naguib Mahfouz',
    });

    expect(dialog['form'].controls.author.value).toEqual({ id: 1, name: 'Naguib Mahfouz' });
    expect(dialog['form'].valid).toBe(true);
  });
});
