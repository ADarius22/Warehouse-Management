import { Component, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { Page, Product, ProductRequest, UNITS } from '../../core/models';
import { AuthService } from '../../core/auth.service';
import { NotificationService } from '../../core/notification.service';
import { ProductService } from '../../core/services';
import { Pagination } from '../../shared/pagination';

@Component({
  selector: 'app-products',
  imports: [ReactiveFormsModule, Pagination],
  templateUrl: './products.html',
})
export class ProductsPage implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  /** Only admins may change master data (the backend enforces this too). */
  protected readonly auth = inject(AuthService);

  protected readonly units = UNITS;
  protected readonly page = signal<Page<Product> | null>(null);
  protected readonly editing = signal<Product | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly saving = signal(false);

  protected readonly search = this.fb.nonNullable.control('');
  private pageIndex = 0;

  protected readonly form = this.fb.group({
    sku: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/^[A-Za-z0-9-]+$/)]],
    name: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', Validators.maxLength(1000)],
    unit: ['PIECE', Validators.required],
    weightKg: [null as number | null, Validators.min(0)],
    minStockLevel: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    // Wait until the user stops typing for 300 ms before calling the API
    this.search.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => {
        this.pageIndex = 0;
        this.load();
      });
  }

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.productService
      .list({ search: this.search.value, page: this.pageIndex, size: 15 })
      .subscribe((p) => this.page.set(p));
  }

  protected goTo(index: number): void {
    this.pageIndex = index;
    this.load();
  }

  protected openCreate(): void {
    this.editing.set(null);
    this.form.reset({ sku: '', name: '', description: '', unit: 'PIECE', weightKg: null, minStockLevel: 0 });
    this.formOpen.set(true);
  }

  protected openEdit(product: Product): void {
    this.editing.set(product);
    this.form.reset({
      sku: product.sku,
      name: product.name,
      description: product.description ?? '',
      unit: product.unit,
      weightKg: product.weightKg,
      minStockLevel: product.minStockLevel,
    });
    this.formOpen.set(true);
  }

  protected invalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && control.touched;
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request = this.form.getRawValue() as ProductRequest;
    const editing = this.editing();
    const call = editing
      ? this.productService.update(editing.id, request)
      : this.productService.create(request);

    this.saving.set(true);
    call.subscribe({
      next: (saved) => {
        this.notifications.success(`Product ${saved.sku} saved`);
        this.formOpen.set(false);
        this.saving.set(false);
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  protected remove(product: Product): void {
    if (!confirm(`Delete product ${product.sku}?`)) {
      return;
    }
    this.productService.delete(product.id).subscribe(() => {
      this.notifications.success(`Product ${product.sku} deleted`);
      this.load();
    });
  }
}
