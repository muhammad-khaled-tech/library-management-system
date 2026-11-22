import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BookList } from './book-list';
import { BookService } from './book.service';

describe('BookList', () => {
  const bookService = {
    search: vi.fn(() =>
      of({
        content: [
          {
            id: 10,
            isbn: '9780306406157',
            title: 'Palace Walk',
            publishedYear: 1956,
            totalCopies: 2,
            availableCopies: 0,
            authorId: 1,
            authorName: 'Naguib Mahfouz',
          },
        ],
        page: { size: 10, number: 0, totalElements: 1, totalPages: 1 },
      }),
    ),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookList],
      providers: [{ provide: BookService, useValue: bookService }],
    });
  });

  it('shows availability and flags books with no copies left', async () => {
    const fixture = TestBed.createComponent(BookList);
    await fixture.whenStable();

    expect(bookService.search).toHaveBeenCalledWith('', { page: 0, size: 10, sort: 'title,asc' });
    const row = (fixture.nativeElement as HTMLElement).querySelector('tr.mat-mdc-row')!;
    expect(row.textContent).toContain('Naguib Mahfouz');
    expect(row.querySelector('td.none-left')?.textContent?.trim()).toBe('0 / 2');
  });
});
