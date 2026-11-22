import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthorList } from './author-list';
import { AuthorService } from './author.service';

describe('AuthorList', () => {
  const authorService = {
    search: vi.fn(() =>
      of({
        content: [
          { id: 1, name: 'Naguib Mahfouz' },
          { id: 2, name: 'Taha Hussein' },
        ],
        page: { size: 10, number: 0, totalElements: 2, totalPages: 1 },
      }),
    ),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuthorList],
      providers: [{ provide: AuthorService, useValue: authorService }],
    });
  });

  it('loads the first page sorted by name and shows each author', async () => {
    const fixture = TestBed.createComponent(AuthorList);
    await fixture.whenStable();

    expect(authorService.search).toHaveBeenCalledWith('', { page: 0, size: 10, sort: 'name,asc' });
    const rows = (fixture.nativeElement as HTMLElement).querySelectorAll('tr.mat-mdc-row');
    expect(Array.from(rows).map((row) => row.textContent)).toEqual([
      expect.stringContaining('Naguib Mahfouz'),
      expect.stringContaining('Taha Hussein'),
    ]);
  });
});
