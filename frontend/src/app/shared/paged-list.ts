import { signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { EMPTY, Observable, Subject, catchError, switchMap } from 'rxjs';
import { Page, PageRequest } from './page';

/**
 * Page, size and sort state for a paged table. Create it in a component field so
 * takeUntilDestroyed() can tie the subscription to the component's lifetime.
 */
export class PagedList<T> {
  readonly page = signal<Page<T> | undefined>(undefined);

  private request: PageRequest;
  private readonly reload = new Subject<void>();

  constructor(fetch: (request: PageRequest) => Observable<Page<T>>, initial: PageRequest) {
    this.request = initial;
    this.reload
      .pipe(
        // switchMap drops an older response that arrives after a newer request;
        // catchError keeps the stream alive after a failed call (the error interceptor reports it)
        switchMap(() => fetch(this.request).pipe(catchError(() => EMPTY))),
        takeUntilDestroyed(),
      )
      .subscribe((page) => this.page.set(page));
  }

  load(): void {
    this.reload.next();
  }

  firstPage(): void {
    this.request = { ...this.request, page: 0 };
    this.load();
  }

  changePage(event: PageEvent): void {
    this.request = { ...this.request, page: event.pageIndex, size: event.pageSize };
    this.load();
  }

  changeSort(sort: Sort): void {
    const order = sort.direction ? `${sort.active},${sort.direction}` : undefined;
    this.request = { ...this.request, page: 0, sort: order };
    this.load();
  }
}
