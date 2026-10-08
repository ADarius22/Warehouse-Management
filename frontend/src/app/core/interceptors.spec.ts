import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { errorInterceptor } from './error.interceptor';
import { NotificationService } from './notification.service';
import { fakeSession, storeSession } from './testing';

describe('HTTP interceptors', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let auth: AuthService;
  let notifications: NotificationService;

  function setup(loggedIn: boolean): void {
    storeSession(loggedIn ? fakeSession('ADMIN') : null);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    notifications = TestBed.inject(NotificationService);
  }

  afterEach(() => {
    backend.verify();
    storeSession(null);
  });

  describe('authInterceptor', () => {
    it('adds the bearer token to API calls', () => {
      setup(true);

      http.get('/api/products').subscribe();

      const req = backend.expectOne('/api/products');
      expect(req.request.headers.get('Authorization')).toBe('Bearer token-admin');
      req.flush({});
    });

    it('sends no token when logged out', () => {
      setup(false);

      http.get('/api/products').subscribe({ error: () => undefined });

      const req = backend.expectOne('/api/products');
      expect(req.request.headers.has('Authorization')).toBeFalse();
      req.flush({}, { status: 401, statusText: 'Unauthorized' });
    });

    it('logs the user out when the server rejects the token', () => {
      setup(true);
      const logout = spyOn(auth, 'logout');

      http.get('/api/products').subscribe({ error: () => undefined });
      backend.expectOne('/api/products').flush({}, { status: 401, statusText: 'Unauthorized' });

      expect(logout).toHaveBeenCalledWith(true);
    });
  });

  describe('errorInterceptor', () => {
    it('shows the problem detail sent by the backend', () => {
      setup(true);
      const error = spyOn(notifications, 'error');

      http.post('/api/orders/1/pick', null).subscribe({ error: () => undefined });
      backend.expectOne('/api/orders/1/pick').flush(
        { status: 409, title: 'Business rule violated', detail: 'Not enough stock for BIN-6040' },
        { status: 409, statusText: 'Conflict' },
      );

      expect(error).toHaveBeenCalledWith('Not enough stock for BIN-6040');
    });

    it('lists field errors of a validation failure', () => {
      setup(true);
      const error = spyOn(notifications, 'error');

      http.post('/api/products', {}).subscribe({ error: () => undefined });
      backend.expectOne('/api/products').flush(
        { status: 400, detail: 'One or more fields are invalid', errors: { sku: 'must not be blank' } },
        { status: 400, statusText: 'Bad Request' },
      );

      expect(error).toHaveBeenCalledWith('One or more fields are invalid: sku must not be blank');
    });

    it('explains when the server cannot be reached', () => {
      setup(true);
      const error = spyOn(notifications, 'error');

      http.get('/api/products').subscribe({ error: () => undefined });
      backend.expectOne('/api/products').error(new ProgressEvent('error'), { status: 0 });

      expect(error.calls.mostRecent().args[0]).toContain('Cannot reach the server');
    });

    it('does not show a toast for 401 (the login flow handles it)', () => {
      setup(true);
      spyOn(auth, 'logout');
      const error = spyOn(notifications, 'error');

      http.get('/api/products').subscribe({ error: () => undefined });
      backend.expectOne('/api/products').flush({}, { status: 401, statusText: 'Unauthorized' });

      expect(error).not.toHaveBeenCalled();
    });
  });
});
