import { Routes } from '@angular/router';
import { PublicLayout } from './layout/public-layout/public-layout';
import { AppLayout } from './layout/app-layout/app-layout';
import { AdminLayout } from './layout/admin-layout/admin-layout';
import { authGuard } from './core/guards/auth/auth.guard';
import { adminGuard } from './core/guards/admin/admin.guard';
import { userGuard } from './core/guards/user/user.guard';
import { landingGuard } from './core/guards/landing/landing.guard';
import { APP_TITLE } from './core/constants';

export const routes: Routes = [
  // Tree 1: PUBLIC PORTAL (Accessible to guests)
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        title: `${APP_TITLE} | Welcome`,
        canActivate: [landingGuard],
        loadComponent: () => import('./features/public-home/public-home').then(m => m.PublicHome)
      },
      { path: 'login', title: `${APP_TITLE} | Login`, loadComponent: () => import('./features/auth/login/login').then(m => m.Login) },
      { path: 'register', title: `${APP_TITLE} | Register`, loadComponent: () => import('./features/auth/register/register').then(m => m.Register) },
      { path: 'books', title: `${APP_TITLE} | Catalog`, loadComponent: () => import('./features/books/book-list/book-list').then(m => m.BookList) },
      { path: 'books/:id', title: `${APP_TITLE} | Book Details`, loadComponent: () => import('./features/books/book-detail/book-detail').then(m => m.BookDetail) }
    ]
  },

  // Tree 2: AUTHENTICATED USER PORTAL (Locked for Admins via userGuard)
  {
    path: 'app',
    component: AppLayout,
    canActivate: [authGuard, userGuard],
    children: [
      { path: 'home', title: `${APP_TITLE} | Dashboard`, loadComponent: () => import('./features/home/home').then(m => m.Home) },
      { path: 'books', title: `${APP_TITLE} | Catalog`, loadComponent: () => import('./features/books/book-list/book-list').then(m => m.BookList) },
      { path: 'my-shelves', title: `${APP_TITLE} | My Shelves`, loadComponent: () => import('./features/shelves/my-shelves/my-shelves').then(m => m.MyShelves) },
      { path: 'profile', title: `${APP_TITLE} | Profile`, loadComponent: () => import('./features/profile/profile').then(m => m.Profile) },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Tree 3: ADMINISTRATIVE PORTAL (Strictly top-level, guarded by adminGuard)
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard, adminGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },

  // Wildcard Fallback
  { path: '**', redirectTo: '' }
];
