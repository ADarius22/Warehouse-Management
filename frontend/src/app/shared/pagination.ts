import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  template: `
    @if (totalPages() > 1) {
      <nav class="pagination" aria-label="Pagination">
        <button class="btn btn-ghost" [disabled]="page() === 0" (click)="pageChange.emit(page() - 1)">‹ Prev</button>
        <span class="muted">Page {{ page() + 1 }} of {{ totalPages() }} · {{ totalElements() }} items</span>
        <button class="btn btn-ghost" [disabled]="page() + 1 >= totalPages()" (click)="pageChange.emit(page() + 1)">
          Next ›
        </button>
      </nav>
    }
  `,
})
export class Pagination {
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly totalElements = input(0);
  readonly pageChange = output<number>();
}
