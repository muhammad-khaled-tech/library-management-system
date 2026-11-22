import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BookRequest } from './book.model';
import { BookService } from './book.service';

describe('BookService', () => {
  let service: BookService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('searches with query, page and sort', () => {
    service.search('palace', { page: 1, size: 10, sort: 'title,asc' }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/books');
    expect(req.request.params.get('q')).toBe('palace');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('sort')).toBe('title,asc');
    req.flush({ content: [], page: { size: 10, number: 1, totalElements: 0, totalPages: 0 } });
  });

  it('updates a book with PUT', () => {
    const body: BookRequest = {
      isbn: '9780306406157',
      title: 'Palace Walk',
      publishedYear: 1956,
      totalCopies: 3,
      authorId: 1,
    };

    service.update(10, body).subscribe();

    const req = http.expectOne('/api/books/10');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('deletes a book', () => {
    service.delete(10).subscribe();

    expect(http.expectOne('/api/books/10').request.method).toBe('DELETE');
  });
});
