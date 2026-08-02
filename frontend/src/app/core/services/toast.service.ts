import {Injectable, signal} from '@angular/core';

/**
 * A single toast notification.
 */
export interface Toast {
  /** The message displayed to the user. */
  message: string;
  /** The visual style of the toast. */
  type: 'success' | 'error';
}

/**
 * Manages transient toast notifications as a reactive signal.
 * Toasts are auto-dismissed after a fixed timeout, and may also be removed manually.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  /** The list of currently visible toasts. */
  toasts = signal<Toast[]>([]);

  /**
   * Adds a toast and schedules its automatic removal after 5 seconds.
   * @param toast The toast to display.
   */
  show(toast: Toast) {
    this.toasts.update(toasts => [...toasts, toast]);
    setTimeout(() => this.remove(toast), 5000);
  }

  /**
   * Displays a success toast.
   * @param message The success message.
   */
  showSuccess(message: string) {
    this.show({ message, type: 'success' });
  }

  /**
   * Displays an error toast.
   * @param message The error message.
   */
  showError(message: string) {
    this.show({ message, type: 'error' });
  }

  /**
   * Removes a specific toast immediately.
   * @param toast The toast to remove.
   */
  remove(toast: Toast) {
    this.toasts.update(toasts => toasts.filter(t => t !== toast));
  }
}
