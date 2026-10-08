import { Routes } from '@angular/router';

// Every page is lazy-loaded: its code is downloaded only when the user navigates to it.
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    title: 'Dashboard · WMS',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.DashboardPage),
  },
  {
    path: 'orders',
    title: 'Orders · WMS',
    loadComponent: () => import('./features/orders/order-list').then((m) => m.OrderListPage),
  },
  {
    path: 'orders/:id',
    title: 'Order · WMS',
    loadComponent: () => import('./features/orders/order-detail').then((m) => m.OrderDetailPage),
  },
  {
    path: 'stock',
    title: 'Stock · WMS',
    loadComponent: () => import('./features/stock/stock').then((m) => m.StockPage),
  },
  {
    path: 'products',
    title: 'Products · WMS',
    loadComponent: () => import('./features/products/products').then((m) => m.ProductsPage),
  },
  {
    path: 'locations',
    title: 'Locations · WMS',
    loadComponent: () => import('./features/locations/locations').then((m) => m.LocationsPage),
  },
  { path: '**', redirectTo: 'dashboard' },
];
