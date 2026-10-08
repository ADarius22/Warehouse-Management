import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { OrderListPage } from './order-list';

const EMPTY_PAGE = { content: [], page: 0, size: 15, totalElements: 0, totalPages: 0 };

describe('OrderListPage', () => {
  let fixture: ComponentFixture<OrderListPage>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [OrderListPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(OrderListPage);
    el = fixture.nativeElement;
    fixture.detectChanges();
    http.expectOne((r) => r.url === '/api/orders').flush(EMPTY_PAGE);
    http.expectOne((r) => r.url === '/api/products').flush({
      ...EMPTY_PAGE,
      content: [
        { id: 1, sku: 'BIN-6040', totalQuantity: 220 },
        { id: 2, sku: 'BOX-M', totalQuantity: 45 },
      ],
    });
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function openForm(): void {
    (el.querySelector('.page-header .btn') as HTMLButtonElement).click();
    fixture.detectChanges();
  }

  function addLineButton(): HTMLButtonElement {
    return Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Add line'))!;
  }

  it('filters the list by status', () => {
    const picked = Array.from(el.querySelectorAll('.tabs button')).find((b) => b.textContent === 'PICKED')!;

    (picked as HTMLButtonElement).click();

    const req = http.expectOne((r) => r.url === '/api/orders');
    expect(req.request.params.get('status')).toBe('PICKED');
    req.flush(EMPTY_PAGE);
  });

  it('adds and removes order lines, keeping at least one', () => {
    openForm();
    expect(el.querySelectorAll('.order-line').length).toBe(1);

    addLineButton().click();
    addLineButton().click();
    fixture.detectChanges();
    expect(el.querySelectorAll('.order-line').length).toBe(3);

    (el.querySelectorAll('.order-line button')[0] as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el.querySelectorAll('.order-line').length).toBe(2);
  });

  it('posts the order and opens it', () => {
    const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    openForm();

    const customer = el.querySelector<HTMLInputElement>('#customer')!;
    customer.value = 'ACME';
    customer.dispatchEvent(new Event('input'));
    const product = el.querySelector<HTMLSelectElement>('.order-line select')!;
    product.selectedIndex = 2; // BOX-M (index 0 is the placeholder)
    product.dispatchEvent(new Event('change'));
    const qty = el.querySelector<HTMLInputElement>('.order-line input')!;
    qty.value = '7';
    qty.dispatchEvent(new Event('input'));
    el.querySelector('form')!.dispatchEvent(new Event('submit'));

    const req = http.expectOne('/api/orders');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ customerName: 'ACME', lines: [{ productId: 2, quantity: 7 }] });
    req.flush({ id: 42, orderNumber: 'ORD-001042' });

    expect(navigate).toHaveBeenCalledWith(['/orders', 42]);
  });

  it('does not submit an order without a product', () => {
    openForm();
    const customer = el.querySelector<HTMLInputElement>('#customer')!;
    customer.value = 'ACME';
    customer.dispatchEvent(new Event('input'));

    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    http.expectNone('/api/orders');
    expect(el.querySelector('.order-line select')!.classList).toContain('ng-invalid');
  });
});
