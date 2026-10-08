import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { fakeSession, storeSession } from './core/testing';

describe('App', () => {
  function render(): HTMLElement {
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  afterEach(() => storeSession(null));

  it('shows navigation and the user when logged in', () => {
    storeSession(fakeSession('OPERATOR'));
    const el = render();

    expect(el.querySelectorAll('.nav a').length).toBe(5);
    expect(el.querySelector('.user-name')?.textContent).toBe('operator');
    expect(el.querySelector('.user-role')?.textContent).toBe('OPERATOR');
  });

  it('hides the navigation when logged out', () => {
    storeSession(null);
    const el = render();

    expect(el.querySelector('.sidebar')).toBeNull();
  });
});
