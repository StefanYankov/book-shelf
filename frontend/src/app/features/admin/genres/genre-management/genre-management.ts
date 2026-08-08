import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {startWith, Subject, switchMap} from 'rxjs';
import {GenreDto, PagedResponseGenreDto} from '../../../../api';
import {ToastService} from '../../../../core/services/toast.service';
import {AdminGenreService} from '../../../../core/services/admin-genre.service';

/**
 * Administrative management page for genres: a paginated list with an inline create/edit form
 * and a per-row delete confirmation. All mutations refresh the list and surface a toast.
 */
@Component({
  selector: 'app-genre-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './genre-management.html',
  styleUrl: './genre-management.css',
})
export class GenreManagement {
  /** The id of the genre being edited, or null when the form is in create mode. */
  protected readonly editingId = signal<string | null>(null);
  /** Whether the create/edit form is visible. */
  protected readonly formOpen = signal(false);
  /** The id awaiting delete confirmation, or null. */
  protected readonly confirmingDeleteId = signal<string | null>(null);
  /** The form heading, reflecting create vs edit mode. */
  protected readonly formTitle = computed(() => (this.editingId() === null ? 'Create Genre' : 'Edit Genre'));
  private readonly genreService = inject(AdminGenreService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    description: ['', [Validators.maxLength(1000)]],
  });
  private readonly refresh$ = new Subject<void>();
  /** The current page of genres, refreshed after each mutation. */
  protected readonly genres = toSignal(
    this.refresh$.pipe(
      startWith(null),
      switchMap(() => this.genreService.getAllGenres({page: 0, size: 100}))
    ),
    {initialValue: {} as PagedResponseGenreDto}
  );

  /** Opens the form in create mode. */
  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.formOpen.set(true);
  }

  /** Opens the form in edit mode, pre-filled from the selected genre. */
  protected startEdit(genre: GenreDto): void {
    this.editingId.set(genre.id ?? null);
    this.form.setValue({name: genre.name ?? '', description: genre.description ?? ''});
    this.formOpen.set(true);
  }

  /** Closes and resets the form. */
  protected cancelForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
    this.form.reset();
  }

  /** Creates or updates a genre depending on the current mode. */
  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id === null
      ? this.genreService.createGenre({name: value.name, description: value.description})
      : this.genreService.updateGenre(id, {name: value.name, description: value.description});

    request$.subscribe({
      next: () => {
        this.toast.showSuccess(id === null ? 'Genre created.' : 'Genre updated.');
        this.cancelForm();
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the genre.'),
    });
  }

  /** Marks a genre for delete confirmation. */
  protected requestDelete(id: string): void {
    this.confirmingDeleteId.set(id);
  }

  /** Cancels a pending delete confirmation. */
  protected cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }

  /** Deletes the confirmed genre and refreshes the list. */
  protected confirmDelete(id: string): void {
    this.genreService.deleteGenre(id).subscribe({
      next: () => {
        this.toast.showSuccess('Genre deleted.');
        this.confirmingDeleteId.set(null);
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to delete the genre.'),
    });
  }
}
