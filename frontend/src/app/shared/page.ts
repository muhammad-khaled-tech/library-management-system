import { HttpParams } from '@angular/common/http';

/** Spring Data's paged response (PagedModel). */
export interface Page<T> {
  content: T[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface PageRequest {
  page: number;
  size: number;
  /** Spring format, e.g. "title,asc". */
  sort?: string;
}

type ParamValue = string | number | null | undefined;

/** Builds query params, leaving out empty filters so the API applies no condition for them. */
export function toParams(values: Record<string, ParamValue>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(values)) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, value);
    }
  }
  return params;
}
