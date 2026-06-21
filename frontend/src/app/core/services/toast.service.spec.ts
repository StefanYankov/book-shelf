import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    vi.useFakeTimers();
    service = new ToastService();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add a success toast and remove it after a timeout', () => {
    // Arrange
    const message = 'Success!';

    // Act
    service.showSuccess(message);

    // Assert: Toast is added immediately
    expect(service.toasts()).toHaveLength(1);
    expect(service.toasts()[0]).toEqual({ message, type: 'success' });

    // Act: Advance timers
    vi.advanceTimersByTime(5000);

    // Assert: Toast is removed after timeout
    expect(service.toasts()).toHaveLength(0);
  });

  it('should add an error toast and allow manual removal', () => {
    // Arrange
    const message = 'Error!';
    service.showError(message);
    const toastToRemove = service.toasts()[0];

    // Assert: Toast is present
    expect(service.toasts()).toHaveLength(1);

    // Act: Manually remove the toast
    service.remove(toastToRemove);

    // Assert: Toast is gone
    expect(service.toasts()).toHaveLength(0);
  });
});
