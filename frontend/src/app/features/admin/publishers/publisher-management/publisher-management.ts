import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {startWith, Subject, switchMap} from 'rxjs';
import {AdminPublisherService} from '../../../../core/services/admin-publisher.service';
import {ToastService} from '../../../../core/services/toast.service';
import {PagedResponsePublisherDto, PublisherDto} from '../../../../api';

/**
 * Administrative management page for publishers: a paginated list with an inline create/edit form
 * and a per-row delete confirmation. All mutations refresh the list and surface a toast.
 */
@Component({
  selector: 'app-publisher-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './publisher-management.html',
  styleUrl: './publisher-management.css',
})
export class PublisherManagement {
  /** The id of the publisher being edited, or null when the form is in create mode. */
  protected readonly editingId = signal<string | null>(null);
  /** Whether the create/edit form is visible. */
  protected readonly formOpen = signal(false);
  /** The id awaiting delete confirmation, or null. */
  protected readonly confirmingDeleteId = signal<string | null>(null);
  /** The form heading, reflecting create vs edit mode. */
  protected readonly formTitle = computed(() => (this.editingId() === null ? 'Create Publisher' : 'Edit Publisher'));
  private readonly publisherService = inject(AdminPublisherService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
  });
  private readonly refresh$ = new Subject<void>();
  /** The current page of publishers, refreshed after each mutation. */
  protected readonly publishers = toSignal(
    this.refresh$.pipe(
      startWith(null),
      switchMap(() => this.publisherService.getAllPublishers({page: 0, size: 100}))
    ),
    {initialValue: {} as PagedResponsePublisherDto}
  );

  /** Opens the form in create mode. */
  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.formOpen.set(true);
  }

  /** Opens the form in edit mode, pre-filled from the selected publisher. */
  protected startEdit(publisher: PublisherDto): void {
    this.editingId.set(publisher.id ?? null);
    this.form.setValue({name: publisher.name ?? ''});
    this.formOpen.set(true);
  }

  /** Closes and resets the form. */
  protected cancelForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
    this.form.reset();
  }

  /** Creates or updates a publisher depending on the current mode. */
  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id === null
      ? this.publisherService.createPublisher({name: value.name})
      : this.publisherService.updatePublisher(id, {name: value.name});

    request$.subscribe({
      next: () => {
        this.toast.showSuccess(id === null ? 'Publisher created.' : 'Publisher updated.');
        this.cancelForm();
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the publisher.'),
    });
  }

  /** Marks a publisher for delete confirmation. */
  protected requestDelete(id: string): void {
    this.confirmingDeleteId.set(id);
  }

  /** Cancels a pending delete confirmation. */
  protected cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }

  /** Deletes the confirmed publisher and refreshes the list. */
  protected confirmDelete(id: string): void {
    this.publisherService.deletePublisher(id).subscribe({
      next: () => {
        this.toast.showSuccess('Publisher deleted.');
        this.confirmingDeleteId.set(null);
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to delete the publisher.'),
    });
  }
}
