import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'books',
    title: 'Books',
    loadComponent: () => import('./books/book-list').then((m) => m.BookList),
  },
  {
    path: 'loans',
    title: 'Loans',
    loadComponent: () => import('./loans/loan-list').then((m) => m.LoanList),
  },
  {
    path: 'authors',
    title: 'Authors',
    loadComponent: () => import('./authors/author-list').then((m) => m.AuthorList),
  },
  {
    path: 'members',
    title: 'Members',
    loadComponent: () => import('./members/member-list').then((m) => m.MemberList),
  },
  { path: '', pathMatch: 'full', redirectTo: 'books' },
  { path: '**', redirectTo: '' },
];
