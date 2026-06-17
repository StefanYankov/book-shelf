import { Routes } from '@angular/router';
import { PublicLayout } from './layout/public-layout/public-layout';
import { AppLayout } from './layout/app-layout/app-layout';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  // Public routes (for guests)
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', redirectTo: 'login', pathMatch: 'full' },
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/login').then(m => m.Login)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register/register').then(m => m.Register)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPassword)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/reset-password/reset-password').then(m => m.ResetPassword)
      },
      {
        path: 'verify-email', // Correct route for email verification
        loadComponent: () => import('./features/auth/verify-email/verify-email').then(m => m.VerifyEmail)
      }
    ]
  },

  // Authenticated routes (for logged-in users)
  {
    path: 'app',
    component: AppLayout,
    canActivate: [authGuard], // Protect this entire section
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/home/home').then(m => m.Home)
      },
      // Default route for authenticated users
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Wildcard route for a 404 page
  { path: '**', redirectTo: 'login' }
];
