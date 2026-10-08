import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CreateOrderRequest, ORDER_STATUSES, OrderStatus, OrderSummary, Page, Product } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { OrderService, ProductService } from '../../core/services';
import { Pagination } from '../../shared/pagination';

@Component({
  selector: 'app-order-list',
  imports: [ReactiveFormsModule, DatePipe, Pagination],
  templateUrl: './order-list.html',
})
export class OrderListPage implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly statuses = ORDER_STATUSES;
  protected readonly page = signal<Page<OrderSummary> | null>(null);
  protected readonly products = signal<Product[]>([]);
  protected readonly status = signal<OrderStatus | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly saving = signal(false);
  private pageIndex = 0;

  protected readonly form = this.fb.group({
    customerName: ['', [Validators.required, Validators.maxLength(200)]],
    lines: this.fb.array([this.newLine()]),
  });

  protected get lines() {
    return this.form.controls.lines;
  }

  ngOnInit(): void {
    this.load();
    this.productService.all().subscribe((p) => this.products.set(p.content));
  }

  protected load(): void {
    this.orderService
      .list({ status: this.status(), page: this.pageIndex, size: 15 })
      .subscribe((p) => this.page.set(p));
  }

  protected filter(status: OrderStatus | null): void {
    this.status.set(status);
    this.pageIndex = 0;
    this.load();
  }

  protected goTo(index: number): void {
    this.pageIndex = index;
    this.load();
  }

  protected open(order: OrderSummary): void {
    this.router.navigate(['/orders', order.id]);
  }

  protected openCreate(): void {
    this.form.reset({ customerName: '' });
    this.lines.clear();
    this.addLine();
    this.formOpen.set(true);
  }

  protected addLine(): void {
    this.lines.push(this.newLine());
  }

  protected removeLine(index: number): void {
    if (this.lines.length > 1) {
      this.lines.removeAt(index);
    }
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue() as CreateOrderRequest;
    this.saving.set(true);
    this.orderService.create(request).subscribe({
      next: (order) => {
        this.notifications.success(`Order ${order.orderNumber} created`);
        this.saving.set(false);
        this.router.navigate(['/orders', order.id]);
      },
      error: () => this.saving.set(false),
    });
  }

  private newLine() {
    return this.fb.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }
}
