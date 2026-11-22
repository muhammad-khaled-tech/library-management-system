import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../shared/crud.service';
import { Page, PageRequest, toParams } from '../shared/page';
import { Book, BookRequest } from './book.model';

@Injectable({ providedIn: 'root' })
export class BookService extends CrudService<Book, BookRequest> {
  protected readonly url = '/api/books';

  /** Matches title, author name or exact ISBN. */
  search(query: string, request: PageRequest): Observable<Page<Book>> {
    return this.http.get<Page<Book>>(this.url, { params: toParams({ q: query, ...request }) });
  }
}
