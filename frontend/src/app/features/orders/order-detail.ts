import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { Order } from '../../core/models';
import { NotificationService } from '../../core/notification.service';
import { OrderService } from '../../core/services';

@Component({
  selector: 'app-order-detail',
  imports: [DatePipe, RouterLink],
  templateUrl: './order-detail.html',
})
export class OrderDetailPage implements OnInit {
  private readonly orderService = inject(OrderService);
  private readonly notifications = inject(NotificationService);

  /** Bound from the ":id" route parameter (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly order = signal<Order | null>(null);
  protected readonly busy = signal(false);
  protected readonly totalUnits = computed(() =>
    (this.order()?.lines ?? []).reduce((sum, line) => sum + line.quantity, 0),
  );

  ngOnInit(): void {
    this.orderService.get(Number(this.id())).subscribe((o) => this.order.set(o));
  }

  protected pick(): void {
    this.run(this.orderService.pick(this.order()!.id), 'picked');
  }

  protected ship(): void {
    this.run(this.orderService.ship(this.order()!.id), 'shipped');
  }

  protected cancel(): void {
    if (confirm('Cancel this order?')) {
      this.run(this.orderService.cancel(this.order()!.id), 'cancelled');
    }
  }

  private run(action: Observable<Order>, verb: string): void {
    this.busy.set(true);
    action.subscribe({
      next: (order) => {
        this.order.set(order);
        this.busy.set(false);
        this.notifications.success(`Order ${order.orderNumber} ${verb}`);
      },
      error: () => this.busy.set(false),
    });
  }
}
