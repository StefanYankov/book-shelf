import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { Configuration, ConfigurationParameters } from './api';
import { BASE_PATH } from './api';

export function apiConfigFactory(): Configuration {
  const params: ConfigurationParameters = {
    basePath: ''
  };
  return new Configuration(params);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),

    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideBrowserGlobalErrorListeners(),

    // --- OpenAPI Configuration Providers ---
    { provide: Configuration, useFactory: apiConfigFactory },
    { provide: BASE_PATH, useValue: '' }
  ]
};
