import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {startWith, Subject, switchMap} from 'rxjs';
import {AdminAuthorService} from '../../../../core/services/admin-author.service';
import {ToastService} from '../../../../core/services/toast.service';
import {AuthorSummaryDto, PagedResponseAuthorSummaryDto} from '../../../../api';

/**
 * Administrative management page for authors: a paginated list with an inline create/edit form
 * and a per-row delete confirmation. Creation accepts an optional profile image; editing updates
 * the name and summary only. All mutations refresh the list and surface a toast.
 */
@Component({
  selector: 'app-author-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './author-management.html',
  styleUrl: './author-management.css',
})
export class AuthorManagement {
  /** The id of the author being edited, or null when the form is in create mode. */
  protected readonly editingId = signal<string | null>(null);
  /** Whether the create/edit form is visible. */
  protected readonly formOpen = signal(false);
  /** The id awaiting delete confirmation, or null. */
  protected readonly confirmingDeleteId = signal<string | null>(null);
  /** The selected image file for a create, or null. Editing does not change the image. */
  protected readonly selectedImage = signal<File | null>(null);
  /** The form heading, reflecting create vs edit mode. */
  protected readonly formTitle = computed(() => (this.editingId() === null ? 'Create Author' : 'Edit Author'));
  /** Whether the image input is shown (create mode only). */
  protected readonly imageInputVisible = computed(() => this.editingId() === null);
  private readonly authorService = inject(AdminAuthorService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    summary: ['', [Validators.maxLength(1000)]],
  });
  private readonly refresh$ = new Subject<void>();
  /** The current page of author summaries, refreshed after each mutation. */
  protected readonly authors = toSignal(
    this.refresh$.pipe(
      startWith(null),
      switchMap(() => this.authorService.getAllAuthors({page: 0, size: 100}))
    ),
    {initialValue: {} as PagedResponseAuthorSummaryDto}
  );

  /** Opens the form in create mode. */
  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.selectedImage.set(null);
    this.formOpen.set(true);
  }

  /** Opens the form in edit mode, pre-filled from the selected author summary. */
  protected startEdit(author: AuthorSummaryDto): void {
    this.editingId.set(author.id ?? null);
    this.form.setValue({name: author.name ?? '', summary: ''});
    this.selectedImage.set(null);
    this.formOpen.set(true);
  }

  /** Records the chosen image file from the file input. */
  protected onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedImage.set(input.files?.[0] ?? null);
  }

  /** Closes and resets the form. */
  protected cancelForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
    this.selectedImage.set(null);
    this.form.reset();
  }

  /** Creates or updates an author depending on the current mode. */
  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id === null
      ? this.authorService.createAuthor({name: value.name, summary: value.summary}, this.selectedImage() ?? undefined)
      : this.authorService.updateAuthor(id, {name: value.name, summary: value.summary});

    request$.subscribe({
      next: () => {
        this.toast.showSuccess(id === null ? 'Author created.' : 'Author updated.');
        this.cancelForm();
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the author.'),
    });
  }

  /** Marks an author for delete confirmation. */
  protected requestDelete(id: string): void {
    this.confirmingDeleteId.set(id);
  }

  /** Cancels a pending delete confirmation. */
  protected cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }

  /** Deletes the confirmed author and refreshes the list. */
  protected confirmDelete(id: string): void {
    this.authorService.deleteAuthor(id).subscribe({
      next: () => {
        this.toast.showSuccess('Author deleted.');
        this.confirmingDeleteId.set(null);
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to delete the author.'),
    });
  }
}
