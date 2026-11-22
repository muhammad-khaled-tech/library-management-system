import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthorService } from './author.service';

describe('AuthorService', () => {
  let service: AuthorService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthorService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('searches authors by name', () => {
    service.search('mahfouz', { page: 0, size: 20 }).subscribe();

    const req = http.expectOne((r) => r.url === '/api/authors');
    expect(req.request.params.get('name')).toBe('mahfouz');
    req.flush({ content: [], page: { size: 20, number: 0, totalElements: 0, totalPages: 0 } });
  });

  it('creates an author with POST', () => {
    service.create({ name: 'Taha Hussein' }).subscribe((author) => expect(author.id).toBe(2));

    const req = http.expectOne('/api/authors');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 2, name: 'Taha Hussein' });
  });
});
