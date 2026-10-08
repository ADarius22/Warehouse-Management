import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { fakeSession, storeSession } from './testing';

describe('AuthService', () => {
  let http: HttpTestingController;

  function create(): AuthService {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(AuthService);
  }

  afterEach(() => {
    storeSession(null);
    http.verify();
  });

  it('starts logged out when nothing is stored', () => {
    storeSession(null);
    const auth = create();

    expect(auth.isLoggedIn()).toBeFalse();
    expect(auth.token()).toBeNull();
  });

  it('logs in, stores the session and exposes the role', () => {
    storeSession(null);
    const auth = create();
    const session = fakeSession('OPERATOR');

    auth.login('operator', 'secret').subscribe();
    const req = http.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'operator', password: 'secret' });
    req.flush(session);

    expect(auth.isLoggedIn()).toBeTrue();
    expect(auth.isAdmin()).toBeFalse();
    expect(auth.token()).toBe(session.token);
    expect(JSON.parse(localStorage.getItem('wms.session')!).username).toBe('operator');
  });

  it('restores a stored session after a page reload', () => {
    storeSession(fakeSession('ADMIN'));
    const auth = create();

    expect(auth.isAdmin()).toBeTrue();
    expect(auth.user()?.username).toBe('admin');
  });

  it('ignores an expired stored session', () => {
    storeSession(fakeSession('ADMIN', -1000));
    const auth = create();

    expect(auth.isLoggedIn()).toBeFalse();
  });

  it('logout clears the session and goes to the login page', () => {
    storeSession(fakeSession('ADMIN'));
    const auth = create();
    const navigate = spyOn(TestBed.inject(Router), 'navigate');

    auth.logout(true);

    expect(auth.isLoggedIn()).toBeFalse();
    expect(localStorage.getItem('wms.session')).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { expired: true } });
  });
});
