import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Page, PageRequest, toParams } from '../shared/page';
import { BorrowRequest, Loan, LoanFilter } from './loan.model';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/loans';

  search(filter: LoanFilter, request: PageRequest): Observable<Page<Loan>> {
    return this.http.get<Page<Loan>>(this.url, { params: toParams({ ...filter, ...request }) });
  }

  borrow(request: BorrowRequest): Observable<Loan> {
    return this.http.post<Loan>(this.url, request);
  }

  returnLoan(id: number): Observable<Loan> {
    return this.http.post<Loan>(`${this.url}/${id}/return`, null);
  }
}
