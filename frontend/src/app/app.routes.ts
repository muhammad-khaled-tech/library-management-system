import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'books',
    title: 'Books',
    loadComponent: () => import('./books/book-list').then((m) => m.BookList),
  },
  {
    path: 'authors',
    title: 'Authors',
    loadComponent: () => import('./authors/author-list').then((m) => m.AuthorList),
  },
  { path: '', pathMatch: 'full', redirectTo: 'books' },
  { path: '**', redirectTo: '' },
];
