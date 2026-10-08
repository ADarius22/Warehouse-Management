import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { fakeSession, storeSession } from '../../core/testing';
import { LoginPage } from './login';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    storeSession(null);
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(LoginPage);
  });

  afterEach(() => {
    http.verify();
    storeSession(null);
  });

  function fill(username: string, password: string): void {
    const el: HTMLElement = fixture.nativeElement;
    const user = el.querySelector<HTMLInputElement>('#username')!;
    const pass = el.querySelector<HTMLInputElement>('#password')!;
    user.value = username;
    user.dispatchEvent(new Event('input'));
    pass.value = password;
    pass.dispatchEvent(new Event('input'));
  }

  function submit(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('logs in and goes to the page the user originally wanted', () => {
    fixture.componentRef.setInput('returnUrl', '/orders/3');
    fixture.detectChanges();

    fill('admin', 'admin123');
    submit();
    http.expectOne('/api/auth/login').flush(fakeSession('ADMIN'));

    expect(router.navigateByUrl).toHaveBeenCalledWith('/orders/3');
  });

  it('never redirects to another website after login', () => {
    fixture.componentRef.setInput('returnUrl', '//evil.example.com');
    fixture.detectChanges();

    fill('admin', 'admin123');
    submit();
    http.expectOne('/api/auth/login').flush(fakeSession('ADMIN'));

    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });

  it('shows an error for wrong credentials', () => {
    fixture.detectChanges();

    fill('admin', 'wrong');
    submit();
    http.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role=alert]').textContent).toContain('Invalid username or password');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('does not call the API when the form is empty', () => {
    fixture.detectChanges();

    submit();

    http.expectNone('/api/auth/login');
    expect(fixture.nativeElement.querySelector('#username').classList).toContain('ng-invalid');
  });

  it('demo buttons fill in the form', () => {
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('.demo-accounts button') as NodeListOf<HTMLButtonElement>;

    buttons[1].click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#username').value).toBe('operator');
  });
});
