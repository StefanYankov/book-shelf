import {Component, computed, inject, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {toSignal} from '@angular/core/rxjs-interop';
import {startWith, Subject, switchMap} from 'rxjs';
import {AdminLanguageService} from '../../../../core/services/admin-language.service';
import {ToastService} from '../../../../core/services/toast.service';
import {LanguageDto, PagedResponseLanguageDto} from '../../../../api';

/**
 * Administrative management page for languages: a paginated list with an inline create/edit form
 * and a per-row delete confirmation. All mutations refresh the list and surface a toast.
 */
@Component({
  selector: 'app-language-management',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './language-management.html',
  styleUrl: './language-management.css',
})
export class LanguageManagement {
  /** The id of the language being edited, or null when the form is in create mode. */
  protected readonly editingId = signal<string | null>(null);
  /** Whether the create/edit form is visible. */
  protected readonly formOpen = signal(false);
  /** The id awaiting delete confirmation, or null. */
  protected readonly confirmingDeleteId = signal<string | null>(null);
  /** The form heading, reflecting create vs edit mode. */
  protected readonly formTitle = computed(() => (this.editingId() === null ? 'Create Language' : 'Edit Language'));
  private readonly languageService = inject(AdminLanguageService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
  });
  private readonly refresh$ = new Subject<void>();
  /** The current page of languages, refreshed after each mutation. */
  protected readonly languages = toSignal(
    this.refresh$.pipe(
      startWith(null),
      switchMap(() => this.languageService.getAllLanguages({page: 0, size: 100}))
    ),
    {initialValue: {} as PagedResponseLanguageDto}
  );

  /** Opens the form in create mode. */
  protected startCreate(): void {
    this.editingId.set(null);
    this.form.reset();
    this.formOpen.set(true);
  }

  /** Opens the form in edit mode, pre-filled from the selected language. */
  protected startEdit(language: LanguageDto): void {
    this.editingId.set(language.id ?? null);
    this.form.setValue({name: language.name ?? ''});
    this.formOpen.set(true);
  }

  /** Closes and resets the form. */
  protected cancelForm(): void {
    this.formOpen.set(false);
    this.editingId.set(null);
    this.form.reset();
  }

  /** Creates or updates a language depending on the current mode. */
  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id === null
      ? this.languageService.createLanguage({name: value.name})
      : this.languageService.updateLanguage(id, {name: value.name});

    request$.subscribe({
      next: () => {
        this.toast.showSuccess(id === null ? 'Language created.' : 'Language updated.');
        this.cancelForm();
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to save the language.'),
    });
  }

  /** Marks a language for delete confirmation. */
  protected requestDelete(id: string): void {
    this.confirmingDeleteId.set(id);
  }

  /** Cancels a pending delete confirmation. */
  protected cancelDelete(): void {
    this.confirmingDeleteId.set(null);
  }

  /** Deletes the confirmed language and refreshes the list. */
  protected confirmDelete(id: string): void {
    this.languageService.deleteLanguage(id).subscribe({
      next: () => {
        this.toast.showSuccess('Language deleted.');
        this.confirmingDeleteId.set(null);
        this.refresh$.next();
      },
      error: (err) => this.toast.showError(err.error?.detail || 'Failed to delete the language.'),
    });
  }
}
