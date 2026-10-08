import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth.interceptor';
import { errorInterceptor } from './core/error.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    // withComponentInputBinding: route and query params (e.g. :id, ?returnUrl) become component inputs
    provideRouter(routes, withComponentInputBinding()),
    // order matters: authInterceptor adds the token first, errorInterceptor shows failures
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
  ],
};
