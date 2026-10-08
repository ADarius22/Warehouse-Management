import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ProblemDetail } from './models';
import { NotificationService } from './notification.service';

/** Shows failed API calls as an error toast, using the backend's problem-detail message. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notifications = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 401 is handled by the auth interceptor (redirect to login) and the login page itself
      if (error.status !== 401) {
        notifications.error(describe(error));
      }
      return throwError(() => error);
    }),
  );
};

function describe(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'Cannot reach the server. Is the backend running on port 8080?';
  }
  const problem = error.error as ProblemDetail | null;
  if (problem?.errors) {
    const fields = Object.entries(problem.errors).map(([field, msg]) => `${field} ${msg}`);
    return `${problem.detail}: ${fields.join(', ')}`;
  }
  return problem?.detail ?? `Request failed (${error.status})`;
}
