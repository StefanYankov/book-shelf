import { Routes } from '@angular/router';
import { APP_TITLE } from "../../core/constants";

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    title: `${APP_TITLE} | Admin Dashboard`,
    loadComponent: () => import('./admin-home/admin-home').then(m => m.AdminHome)
  },
  {
    path: 'users',
    title: `${APP_TITLE} | User Management`,
    loadComponent: () => import('./users/user-list/user-list').then(m => m.UserList)
  },
  {
    path: 'profile',
    title: `${APP_TITLE} | Admin Security`,
    loadComponent: () => import('./profile/admin-profile').then(m => m.AdminProfile)
  },
  {
    path: 'moderation',
    title: `${APP_TITLE} | Content Moderation`,
    loadComponent: () => import('./moderation/content-moderation').then(m => m.ContentModeration)
  }
];
