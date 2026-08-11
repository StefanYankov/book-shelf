import {Route} from '@angular/router';
import {describe, expect, it} from 'vitest';
import {routes} from './app.routes';
import {APP_TITLE} from './core/constants';

describe('Application Route Configuration', () => {

  /**
   * Returns the authenticated application route.
   *
   * @returns The `/app` route configuration.
   * @throws Error when the authenticated route is missing.
   */
  const getAuthenticatedAppRoute = (): Route => {
    const appRoute = routes.find(route => route.path === 'app');

    if (!appRoute) {
      throw new Error('Authenticated /app route is missing.');
    }

    return appRoute;
  };

  it('should register the shelf-details route under the authenticated application route', () => {
    // Arrange
    const appRoute = getAuthenticatedAppRoute();

    // Act
    const shelfDetailRoute = appRoute.children?.find(
      route => route.path === 'shelves/:id'
    );

    // Assert
    expect(shelfDetailRoute).toBeDefined();
    expect(shelfDetailRoute?.title).toBe(`${APP_TITLE} | Shelf Details`);
    expect(shelfDetailRoute?.loadComponent).toBeTypeOf('function');
  });

  it('should retain the my-shelves list route under the authenticated application route', () => {
    // Arrange
    const appRoute = getAuthenticatedAppRoute();

    // Act
    const myShelvesRoute = appRoute.children?.find(
      route => route.path === 'my-shelves'
    );

    // Assert
    expect(myShelvesRoute).toBeDefined();
    expect(myShelvesRoute?.title).toBe(`${APP_TITLE} | My Shelves`);
    expect(myShelvesRoute?.loadComponent).toBeTypeOf('function');
  });

  it('should keep the shelf-details route alongside the my-shelves route', () => {
    // Arrange
    const appRoute = getAuthenticatedAppRoute();
    const authenticatedPaths = appRoute.children?.map(route => route.path);

    // Act
    const hasShelfListRoute = authenticatedPaths?.includes('my-shelves');
    const hasShelfDetailRoute = authenticatedPaths?.includes('shelves/:id');

    // Assert
    expect(hasShelfListRoute).toBe(true);
    expect(hasShelfDetailRoute).toBe(true);
  });

  it('should keep the default authenticated redirect after the concrete routes', () => {
    // Arrange
    const appRoute = getAuthenticatedAppRoute();

    // Act
    const defaultRoute = appRoute.children?.find(
      route => route.path === ''
    );

    // Assert
    expect(defaultRoute).toEqual({
      path: '',
      redirectTo: 'home',
      pathMatch: 'full'
    });
  });
});
