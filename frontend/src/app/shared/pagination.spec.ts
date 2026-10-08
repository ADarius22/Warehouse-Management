import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Pagination } from './pagination';

describe('Pagination', () => {
  let fixture: ComponentFixture<Pagination>;

  function render(page: number, totalPages: number): HTMLElement {
    fixture = TestBed.createComponent(Pagination);
    fixture.componentRef.setInput('page', page);
    fixture.componentRef.setInput('totalPages', totalPages);
    fixture.componentRef.setInput('totalElements', totalPages * 10);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  it('is hidden when everything fits on one page', () => {
    expect(render(0, 1).querySelector('nav')).toBeNull();
  });

  it('disables Prev on the first page and emits the next page', () => {
    const el = render(0, 3);
    const [prev, next] = Array.from(el.querySelectorAll('button')) as HTMLButtonElement[];
    let emitted: number | undefined;
    fixture.componentInstance.pageChange.subscribe((p) => (emitted = p));

    expect(prev.disabled).toBeTrue();
    expect(el.textContent).toContain('Page 1 of 3');

    next.click();
    expect(emitted).toBe(1);
  });

  it('disables Next on the last page', () => {
    const el = render(2, 3);
    const next = el.querySelectorAll('button')[1] as HTMLButtonElement;

    expect(next.disabled).toBeTrue();
  });
});
