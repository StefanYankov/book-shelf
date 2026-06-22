import { Routes } from '@angular/router';
import { PublicLayout } from './layout/public-layout/public-layout';
import { AppLayout } from './layout/app-layout/app-layout';
import { authGuard } from './core/auth.guard';
import { APP_TITLE } from './core/constants';

export const routes: Routes = [
  // Public routes (for guests)
  {
    path: '',
    component: PublicLayout,
    title: APP_TITLE,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'login',
        title: `${APP_TITLE} | Login`,
        loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
      },
      {
        path: 'register',
        title: `${APP_TITLE} | Register`,
        loadComponent: () => import('./features/auth/register/register').then(m => m.Register)
      },
      {
        path: 'forgot-password',
        title: `${APP_TITLE} | Forgot Password`,
        loadComponent: () => import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPassword)
      },
      {
        path: 'reset-password',
        title: `${APP_TITLE} | Reset Password`,
        loadComponent: () => import('./features/auth/reset-password/reset-password').then(m => m.ResetPassword)
      },
      {
        path: 'verify-email',
        title: `${APP_TITLE} | Verifying Email`,
        loadComponent: () => import('./features/auth/verify-email/verify-email').then(m => m.VerifyEmail)
      },
      {
        path: 'books',
        title: `${APP_TITLE} | Explore Books`,
        loadComponent: () => import('./features/books/book-list/book-list').then(m => m.BookList)
      }
    ]
  },

  // Authenticated routes (for logged-in users)
  {
    path: 'app',
    component: AppLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        title: `${APP_TITLE} | Home`,
        loadComponent: () => import('./features/home/home').then(m => m.Home)
      },
      {
        path: 'books',
        title: `${APP_TITLE} | Explore Books`,
        loadComponent: () => import('./features/books/book-list/book-list').then(m => m.BookList)
      },
      {
        path: 'books/:id',
        title: `${APP_TITLE} | Book Details`,
        loadComponent: () => import('./features/books/book-detail/book-detail').then(m => m.BookDetail)
      },
      {
        path: 'my-shelves',
        title: `${APP_TITLE} | My Shelves`,
        loadComponent: () => import('./features/shelves/my-shelves/my-shelves').then(m => m.MyShelves)
      },
      {
        path: 'shelves/:id',
        title: `${APP_TITLE} | Shelf Details`,
        loadComponent: () => import('./features/shelves/shelf-detail/shelf-detail').then(m => m.ShelfDetail)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Wildcard route for a 404 page
  { path: '**', redirectTo: 'login' }
];
