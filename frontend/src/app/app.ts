import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Toasts } from './shared/toasts';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Toasts],
  templateUrl: './app.html',
})
export class App {
  protected readonly links = [
    { path: '/dashboard', label: 'Dashboard', icon: '◧' },
    { path: '/orders', label: 'Orders', icon: '⇪' },
    { path: '/stock', label: 'Stock', icon: '▤' },
    { path: '/products', label: 'Products', icon: '◫' },
    { path: '/locations', label: 'Locations', icon: '⌗' },
  ];
}
