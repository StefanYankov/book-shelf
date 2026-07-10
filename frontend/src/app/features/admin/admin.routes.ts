import { Routes } from '@angular/router';
import { APP_TITLE } from '../../core/constants';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'users',
    pathMatch: 'full'
  },
  {
    path: 'users',
    title: `${APP_TITLE} | User Management`,
    loadComponent: () => import('./users/user-list/user-list').then(m => m.UserList)
  }
];
