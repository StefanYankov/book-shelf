import {Routes} from '@angular/router';
import {APP_TITLE} from "../../core/constants";

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
    path: 'authors',
    title: `${APP_TITLE} | Author Management`,
    loadComponent: () => import('./authors/author-management/author-management').then(m => m.AuthorManagement)
  },
  {
    path: 'genres',
    title: `${APP_TITLE} | Genre Management`,
    loadComponent: () => import('./genres/genre-management/genre-management').then(m => m.GenreManagement)
  },
  {
    path: 'languages',
    title: `${APP_TITLE} | Language Management`,
    loadComponent: () => import('./languages/language-management/language-management').then(m => m.LanguageManagement)
  },
  {
    path: 'publishers',
    title: `${APP_TITLE} | Publisher Management`,
    loadComponent: () => import('./publishers/publisher-management/publisher-management').then(m => m.PublisherManagement)
  },
  {
    path: 'books',
    title: `${APP_TITLE} | Book Management`,
    loadComponent: () => import('./books/book-management/book-management').then(m => m.BookManagement)
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
