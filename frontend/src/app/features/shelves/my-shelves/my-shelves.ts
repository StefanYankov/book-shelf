import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookshelfService } from '../../../core/services/bookshelf.service';
import { ToastService } from '../../../core/services/toast.service';
import { PagedResponseBookshelfSummaryDto } from '../../../api';
import { Subject, switchMap, startWith } from 'rxjs';

@Component({
  selector: 'app-my-shelves',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './my-shelves.html',
  styleUrls: ['./my-shelves.css'],
})
export class MyShelves {
  private readonly bookshelfService = inject(BookshelfService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  private readonly refreshSubject = new Subject<void>();

  shelves = toSignal(
    this.refreshSubject.pipe(
      startWith(null),
      switchMap(() => this.bookshelfService.getShelvesForUser({ page: 0, size: 20 }))
    ),
    { initialValue: {} as PagedResponseBookshelfSummaryDto }
  );

  isCreating = signal(false);

  createForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]]
  });

  toggleCreateForm() {
    this.isCreating.set(!this.isCreating());
    if (!this.isCreating()) {
      this.createForm.reset();
    }
  }

  onSubmit() {
    if (this.createForm.invalid) {
      return;
    }

    const { name, description } = this.createForm.getRawValue();

    this.bookshelfService.createShelf({ name, description }).subscribe({
      next: () => {
        this.toastService.showSuccess('Shelf created successfully!');
        this.toggleCreateForm();
        this.refreshSubject.next();
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to create shelf.');
      }
    });
  }
}
