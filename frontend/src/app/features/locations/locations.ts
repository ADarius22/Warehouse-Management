import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LocationRequest, Page, StorageLocation, Zone, ZONES } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { LocationService } from '../../core/services';
import { Pagination } from '../../shared/pagination';

@Component({
  selector: 'app-locations',
  imports: [ReactiveFormsModule, Pagination],
  templateUrl: './locations.html',
})
export class LocationsPage implements OnInit {
  private readonly locationService = inject(LocationService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  protected readonly zones = ZONES;
  protected readonly page = signal<Page<StorageLocation> | null>(null);
  protected readonly editing = signal<StorageLocation | null>(null);
  protected readonly formOpen = signal(false);
  protected readonly saving = signal(false);

  protected zoneFilter: Zone | null = null;
  private pageIndex = 0;

  protected readonly form = this.fb.group({
    zone: ['STORAGE', Validators.required],
    aisle: ['', [Validators.required, Validators.maxLength(10), Validators.pattern(/^[A-Za-z0-9]+$/)]],
    rack: [1, [Validators.required, Validators.min(1), Validators.max(99)]],
    shelf: [1, [Validators.required, Validators.min(1), Validators.max(99)]],
    capacity: [100, [Validators.required, Validators.min(1)]],
    active: [true],
  });

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.locationService
      .list({ zone: this.zoneFilter, page: this.pageIndex, size: 20 })
      .subscribe((p) => this.page.set(p));
  }

  protected filterZone(zone: Zone | null): void {
    this.zoneFilter = zone;
    this.pageIndex = 0;
    this.load();
  }

  protected goTo(index: number): void {
    this.pageIndex = index;
    this.load();
  }

  protected openCreate(): void {
    this.editing.set(null);
    this.form.reset({ zone: 'STORAGE', aisle: '', rack: 1, shelf: 1, capacity: 100, active: true });
    this.formOpen.set(true);
  }

  protected openEdit(location: StorageLocation): void {
    this.editing.set(location);
    this.form.reset({
      zone: location.zone,
      aisle: location.aisle,
      rack: location.rack,
      shelf: location.shelf,
      capacity: location.capacity,
      active: location.active,
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
    const request = this.form.getRawValue() as LocationRequest;
    const editing = this.editing();
    const call = editing
      ? this.locationService.update(editing.id, request)
      : this.locationService.create(request);

    this.saving.set(true);
    call.subscribe({
      next: (saved) => {
        this.notifications.success(`Location ${saved.code} saved`);
        this.formOpen.set(false);
        this.saving.set(false);
        this.load();
      },
      error: () => this.saving.set(false),
    });
  }

  protected remove(location: StorageLocation): void {
    if (!confirm(`Delete location ${location.code}?`)) {
      return;
    }
    this.locationService.delete(location.id).subscribe(() => {
      this.notifications.success(`Location ${location.code} deleted`);
      this.load();
    });
  }
}
