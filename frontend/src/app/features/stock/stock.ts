import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  Movement,
  Page,
  Product,
  ReceiveStockRequest,
  StockItem,
  StorageLocation,
  TransferStockRequest,
} from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { LocationService, ProductService, StockService } from '../../core/services';
import { Pagination } from '../../shared/pagination';

type Tab = 'inventory' | 'movements';

@Component({
  selector: 'app-stock',
  imports: [ReactiveFormsModule, FormsModule, DatePipe, Pagination],
  templateUrl: './stock.html',
})
export class StockPage implements OnInit {
  private readonly stockService = inject(StockService);
  private readonly productService = inject(ProductService);
  private readonly locationService = inject(LocationService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  protected readonly tab = signal<Tab>('inventory');
  protected readonly stock = signal<Page<StockItem> | null>(null);
  protected readonly movements = signal<Page<Movement> | null>(null);
  protected readonly products = signal<Product[]>([]);
  protected readonly locations = signal<StorageLocation[]>([]);
  protected readonly busy = signal(false);

  // Filters (template-driven with ngModel: simple two-way binding)
  protected productFilter: number | null = null;
  protected locationFilter: number | null = null;
  private stockPage = 0;
  private movementPage = 0;

  protected readonly receiveForm = this.fb.group({
    productId: [null as number | null, Validators.required],
    locationId: [null as number | null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    reference: [''],
  });

  protected readonly transferForm = this.fb.group({
    productId: [null as number | null, Validators.required],
    fromLocationId: [null as number | null, Validators.required],
    toLocationId: [null as number | null, Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    reference: [''],
  });

  ngOnInit(): void {
    this.productService.all().subscribe((p) => this.products.set(p.content));
    this.locationService.all().subscribe((p) => this.locations.set(p.content.filter((l) => l.active)));
    this.loadStock();
    this.loadMovements();
  }

  protected applyFilters(): void {
    this.stockPage = 0;
    this.movementPage = 0;
    this.loadStock();
    this.loadMovements();
  }

  protected loadStock(page = this.stockPage): void {
    this.stockPage = page;
    this.stockService
      .search({ productId: this.productFilter, locationId: this.locationFilter, page, size: 15 })
      .subscribe((p) => this.stock.set(p));
  }

  protected loadMovements(page = this.movementPage): void {
    this.movementPage = page;
    this.stockService
      .movements({ productId: this.productFilter, page, size: 15 })
      .subscribe((p) => this.movements.set(p));
  }

  protected receive(): void {
    if (this.receiveForm.invalid) {
      this.receiveForm.markAllAsTouched();
      return;
    }
    const request = this.receiveForm.getRawValue() as ReceiveStockRequest;
    this.busy.set(true);
    this.stockService.receive(request).subscribe({
      next: (item) => {
        this.notifications.success(`Received ${request.quantity} × ${item.sku} into ${item.locationCode}`);
        this.receiveForm.reset({ productId: null, locationId: null, quantity: 1, reference: '' });
        this.afterChange();
      },
      error: () => this.busy.set(false),
    });
  }

  protected transfer(): void {
    if (this.transferForm.invalid) {
      this.transferForm.markAllAsTouched();
      return;
    }
    const request = this.transferForm.getRawValue() as TransferStockRequest;
    this.busy.set(true);
    this.stockService.transfer(request).subscribe({
      next: (item) => {
        this.notifications.success(`Moved ${request.quantity} × ${item.sku} to ${item.locationCode}`);
        this.transferForm.reset({ productId: null, fromLocationId: null, toLocationId: null, quantity: 1, reference: '' });
        this.afterChange();
      },
      error: () => this.busy.set(false),
    });
  }

  /** Pre-fills the transfer form from a row of the inventory table. */
  protected startTransfer(item: StockItem): void {
    this.transferForm.patchValue({ productId: item.productId, fromLocationId: item.locationId, quantity: item.quantity });
  }

  private afterChange(): void {
    this.busy.set(false);
    this.loadStock();
    this.loadMovements();
    this.locationService.all().subscribe((p) => this.locations.set(p.content.filter((l) => l.active)));
  }
}
