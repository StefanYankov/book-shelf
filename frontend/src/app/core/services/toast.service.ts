import { Injectable, signal } from '@angular/core';

export interface Toast {
  message: string;
  type: 'success' | 'error';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  toasts = signal<Toast[]>([]);

  show(toast: Toast) {
    this.toasts.update(toasts => [...toasts, toast]);
    setTimeout(() => this.remove(toast), 5000);
  }

  showSuccess(message: string) {
    this.show({ message, type: 'success' });
  }

  showError(message: string) {
    this.show({ message, type: 'error' });
  }

  remove(toast: Toast) {
    this.toasts.update(toasts => toasts.filter(t => t !== toast));
  }
}
