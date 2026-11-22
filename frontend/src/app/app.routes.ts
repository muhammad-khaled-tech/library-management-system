import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'authors',
    title: 'Authors',
    loadComponent: () => import('./authors/author-list').then((m) => m.AuthorList),
  },
  { path: '', pathMatch: 'full', redirectTo: 'authors' },
  { path: '**', redirectTo: '' },
];
