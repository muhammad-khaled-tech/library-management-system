import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../shared/crud.service';
import { Page, PageRequest, toParams } from '../shared/page';
import { Author, AuthorRequest } from './author.model';

@Injectable({ providedIn: 'root' })
export class AuthorService extends CrudService<Author, AuthorRequest> {
  protected readonly url = '/api/authors';

  search(name: string, request: PageRequest): Observable<Page<Author>> {
    return this.http.get<Page<Author>>(this.url, { params: toParams({ name, ...request }) });
  }
}
