import { Component, inject } from '@angular/core';
import { NotificationService } from '../core/notification.service';

@Component({
  selector: 'app-toasts',
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of notifications.toasts(); track toast.id) {
        <div class="toast" [class.toast-error]="toast.kind === 'error'" (click)="notifications.dismiss(toast.id)">
          {{ toast.text }}
        </div>
      }
    </div>
  `,
})
export class Toasts {
  protected readonly notifications = inject(NotificationService);
}
