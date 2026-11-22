import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CrudService } from '../shared/crud.service';
import { Page, PageRequest, toParams } from '../shared/page';
import { Member, MemberRequest } from './member.model';

@Injectable({ providedIn: 'root' })
export class MemberService extends CrudService<Member, MemberRequest> {
  protected readonly url = '/api/members';

  /** Matches name or email. */
  search(query: string, request: PageRequest): Observable<Page<Member>> {
    return this.http.get<Page<Member>>(this.url, { params: toParams({ q: query, ...request }) });
  }
}
