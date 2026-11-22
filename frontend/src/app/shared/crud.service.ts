import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';

/** The get/create/update/delete calls that authors, books and members share. */
export abstract class CrudService<T, TRequest> {
  protected readonly http = inject(HttpClient);
  protected abstract readonly url: string;

  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.url}/${id}`);
  }

  create(body: TRequest): Observable<T> {
    return this.http.post<T>(this.url, body);
  }

  update(id: number, body: TRequest): Observable<T> {
    return this.http.put<T>(`${this.url}/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
