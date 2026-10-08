import { Component, inject, input, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class LoginPage implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  /** Query params, bound by withComponentInputBinding. */
  readonly returnUrl = input<string>();
  readonly expired = input<string>();

  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  ngOnInit(): void {
    if (this.auth.isLoggedIn()) {
      this.router.navigateByUrl(this.target());
    } else if (this.expired()) {
      this.error.set('Your session has expired. Please log in again.');
    }
  }

  /** Fills the form with one of the demo accounts. */
  protected useDemo(username: string, password: string): void {
    this.form.setValue({ username, password });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { username, password } = this.form.getRawValue();
    this.busy.set(true);
    this.error.set(null);
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigateByUrl(this.target()),
      error: (err) => {
        this.busy.set(false);
        this.error.set(err.status === 401 ? 'Invalid username or password.' : 'Login failed. Is the backend running?');
      },
    });
  }

  private target(): string {
    const url = this.returnUrl();
    // only allow in-app paths, never redirect to another site
    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/dashboard';
  }
}
