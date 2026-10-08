import { Role, Session } from './auth.service';

/** Test helpers shared by the spec files. */

export function fakeSession(role: Role = 'ADMIN', expiresInMs = 60 * 60 * 1000): Session {
  return {
    token: `token-${role.toLowerCase()}`,
    username: role.toLowerCase(),
    roles: [role],
    expiresAt: new Date(Date.now() + expiresInMs).toISOString(),
  };
}

/** Puts a session in localStorage so AuthService starts logged in. */
export function storeSession(session: Session | null): void {
  if (session) {
    localStorage.setItem('wms.session', JSON.stringify(session));
  } else {
    localStorage.removeItem('wms.session');
  }
}
