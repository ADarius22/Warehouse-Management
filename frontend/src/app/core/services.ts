import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { toParams } from './http-params';
import {
  CreateOrderRequest,
  Dashboard,
  LocationRequest,
  LowStockItem,
  Movement,
  Order,
  OrderStatus,
  OrderSummary,
  Page,
  Product,
  ProductRequest,
  ReceiveStockRequest,
  StockItem,
  StorageLocation,
  TransferStockRequest,
  Zone,
} from './models';

// One service per REST resource. URLs are relative: in development the Angular dev server
// proxies /api to the Spring Boot backend (see proxy.conf.json).

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);

  list(filter: { search?: string; page?: number; size?: number }): Observable<Page<Product>> {
    return this.http.get<Page<Product>>('/api/products', { params: toParams(filter) });
  }

  /** All products, for drop-downs. */
  all(): Observable<Page<Product>> {
    return this.list({ size: 500 });
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>('/api/products', request);
  }

  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`/api/products/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/products/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class LocationService {
  private readonly http = inject(HttpClient);

  list(filter: { zone?: Zone | null; search?: string; page?: number; size?: number }): Observable<Page<StorageLocation>> {
    return this.http.get<Page<StorageLocation>>('/api/locations', { params: toParams(filter) });
  }

  all(): Observable<Page<StorageLocation>> {
    return this.list({ size: 500 });
  }

  create(request: LocationRequest): Observable<StorageLocation> {
    return this.http.post<StorageLocation>('/api/locations', request);
  }

  update(id: number, request: LocationRequest): Observable<StorageLocation> {
    return this.http.put<StorageLocation>(`/api/locations/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`/api/locations/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly http = inject(HttpClient);

  search(filter: { productId?: number | null; locationId?: number | null; page?: number; size?: number }): Observable<Page<StockItem>> {
    return this.http.get<Page<StockItem>>('/api/stock', { params: toParams(filter) });
  }

  receive(request: ReceiveStockRequest): Observable<StockItem> {
    return this.http.post<StockItem>('/api/stock/receive', request);
  }

  transfer(request: TransferStockRequest): Observable<StockItem> {
    return this.http.post<StockItem>('/api/stock/transfer', request);
  }

  lowStock(): Observable<LowStockItem[]> {
    return this.http.get<LowStockItem[]>('/api/stock/low-stock');
  }

  movements(filter: { productId?: number | null; page?: number; size?: number }): Observable<Page<Movement>> {
    return this.http.get<Page<Movement>>('/api/stock/movements', { params: toParams(filter) });
  }
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);

  list(filter: { status?: OrderStatus | null; page?: number; size?: number }): Observable<Page<OrderSummary>> {
    return this.http.get<Page<OrderSummary>>('/api/orders', { params: toParams(filter) });
  }

  get(id: number): Observable<Order> {
    return this.http.get<Order>(`/api/orders/${id}`);
  }

  create(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>('/api/orders', request);
  }

  pick(id: number): Observable<Order> {
    return this.http.post<Order>(`/api/orders/${id}/pick`, null);
  }

  ship(id: number): Observable<Order> {
    return this.http.post<Order>(`/api/orders/${id}/ship`, null);
  }

  cancel(id: number): Observable<Order> {
    return this.http.post<Order>(`/api/orders/${id}/cancel`, null);
  }
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  get(): Observable<Dashboard> {
    return this.http.get<Dashboard>('/api/dashboard');
  }
}
