import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

export type Role = 'ADMIN' | 'OPERATOR';

/** Response of POST /api/auth/login. */
export interface Session {
  token: string;
  username: string;
  roles: Role[];
  expiresAt: string;
}

const STORAGE_KEY = 'wms.session';

/**
 * Holds the logged-in user and their JWT.
 * The session is kept in localStorage so a page refresh does not log the user out.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(restore());

  readonly user = this.session.asReadonly();
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly isAdmin = computed(() => this.session()?.roles.includes('ADMIN') ?? false);

  login(username: string, password: string): Observable<Session> {
    return this.http
      .post<Session>('/api/auth/login', { username, password })
      .pipe(tap((session) => this.store(session)));
  }

  /** The current token, or null when logged out or the token has expired. */
  token(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (isExpired(session)) {
      this.store(null);
      return null;
    }
    return session.token;
  }

  /** @param expired true when the server rejected the token, so the login page can explain why. */
  logout(expired = false): void {
    this.store(null);
    this.router.navigate(['/login'], expired ? { queryParams: { expired: true } } : {});
  }

  private store(session: Session | null): void {
    this.session.set(session);
    try {
      if (session) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      } else {
        localStorage.removeItem(STORAGE_KEY);
      }
    } catch {
      // storage unavailable (private mode): the session simply lasts until the page is closed
    }
  }
}

function restore(): Session | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    const session = raw ? (JSON.parse(raw) as Session) : null;
    return session && !isExpired(session) ? session : null;
  } catch {
    return null;
  }
}

function isExpired(session: Session): boolean {
  return new Date(session.expiresAt).getTime() <= Date.now();
}
