import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Role } from '../../core/auth.service';
import { Page, Product } from '../../core/models';
import { fakeSession, storeSession } from '../../core/testing';
import { ProductsPage } from './products';

const PRODUCTS: Page<Product> = {
  content: [
    {
      id: 1, sku: 'BIN-6040', name: 'Plastic bin', description: null, unit: 'PIECE', weightKg: 1.9,
      minStockLevel: 50, totalQuantity: 220, lowStock: false, createdAt: '', updatedAt: '',
    },
    {
      id: 2, sku: 'SCAN-2D', name: 'Scanner', description: null, unit: 'PIECE', weightKg: 0.35,
      minStockLevel: 5, totalQuantity: 3, lowStock: true, createdAt: '', updatedAt: '',
    },
  ],
  page: 0, size: 15, totalElements: 2, totalPages: 1,
};

describe('ProductsPage', () => {
  afterEach(() => storeSession(null));

  function render(role: Role) {
    storeSession(fakeSession(role));
    TestBed.configureTestingModule({
      imports: [ProductsPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(ProductsPage);
    fixture.detectChanges();
    http.expectOne((r) => r.url === '/api/products').flush(PRODUCTS);
    fixture.detectChanges();
    return { el: fixture.nativeElement as HTMLElement, http };
  }

  it('lists products and flags low stock', () => {
    const { el } = render('ADMIN');

    const rows = el.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[1].textContent).toContain('SCAN-2D');
    expect(rows[1].querySelector('.badge-low')).not.toBeNull();
  });

  it('shows create, edit and delete actions to admins', () => {
    const { el } = render('ADMIN');

    expect(el.querySelector('.page-header .btn')?.textContent).toContain('New product');
    expect(el.querySelectorAll('td.actions button').length).toBe(4);
  });

  it('hides master-data actions from operators', () => {
    const { el } = render('OPERATOR');

    expect(el.querySelector('.page-header .btn')).toBeNull();
    expect(el.querySelectorAll('td.actions button').length).toBe(0);
  });
});
