import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { ToastComponent } from './toast';
import { ToastService } from '../../../core/services/toast.service';

describe('ToastComponent', () => {
  let component: ToastComponent;
  let fixture: ComponentFixture<ToastComponent>;
  let toastService: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ToastComponent],
      providers: [ToastService]
    }).compileComponents();

    fixture = TestBed.createComponent(ToastComponent);
    component = fixture.componentInstance;
    toastService = TestBed.inject(ToastService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render toasts from the ToastService', () => {
    // Arrange
    toastService.showSuccess('Test Success');
    fixture.detectChanges();

    // Assert
    const compiled = fixture.nativeElement as HTMLElement;
    const toastElement = compiled.querySelector('.toast');
    expect(toastElement).not.toBeNull();
    expect(toastElement?.textContent).toContain('Test Success');
    expect(toastElement?.classList.contains('text-bg-success')).toBe(true);
  });

  it('should call remove on the service when the close button is clicked', () => {
    // Arrange
    const removeSpy = vi.spyOn(toastService, 'remove');
    const toast = { message: 'Test', type: 'error' as const };
    toastService.show(toast);
    fixture.detectChanges();

    // Act
    const closeButton = fixture.nativeElement.querySelector('.btn-close') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    // Assert
    expect(removeSpy).toHaveBeenCalledWith(toast);
  });
});
