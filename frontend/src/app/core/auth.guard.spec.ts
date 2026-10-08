import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, provideRouter, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { fakeSession, storeSession } from './testing';

describe('authGuard', () => {
  function runGuard(url: string) {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
    return TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    );
  }

  afterEach(() => storeSession(null));

  it('allows a logged-in user', () => {
    storeSession(fakeSession('OPERATOR'));

    expect(runGuard('/stock')).toBeTrue();
  });

  it('redirects to login and remembers where the user wanted to go', () => {
    storeSession(null);

    const result = runGuard('/orders/7') as UrlTree;

    expect(result.toString()).toBe('/login?returnUrl=%2Forders%2F7');
  });
});
