import { Component, computed, effect, inject, input, output } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { signal } from '@angular/core';
import {ReviewService, ReviewTargetType} from '../../../core/services/review.service';
import { ToastService } from '../../../core/services/toast.service';
import { ReviewViewDto } from '../../../api';

/**
 * A reactive form for creating or editing a review.
 * <p>
 * The form is mode-agnostic: pass a {@link reviewToEdit} to switch it into edit mode
 * (it prefills and calls update); otherwise it operates in add mode (calls create).
 * The parent decides which mode to render — this component just performs the action
 * it is given. On success it emits {@link saved} so the parent can refresh its list.
 */
@Component({
  selector: 'app-review-form',
  imports: [ReactiveFormsModule],
  templateUrl: './review-form.html',
  styleUrls: ['./review-form.css'],
})
export class ReviewForm {
  private readonly fb = inject(FormBuilder);
  private readonly reviewService = inject(ReviewService);
  private readonly toastService = inject(ToastService);

  // Client-side bounds mirror the backend's ValidationConstants.Review.
  private static readonly MAX_TITLE_LENGTH = 100;
  private static readonly MAX_COMMENT_LENGTH = 2000;

  /** The target being reviewed (e.g. a book id). Required. */
  readonly targetId = input.required<string>();
  /** The kind of target; defaults to BOOK. */
  readonly targetType = input<ReviewTargetType>('BOOK');
  /** When provided, the form edits this review; otherwise it creates a new one. */
  readonly reviewToEdit = input<ReviewViewDto | null>(null);

  /** Emitted after a successful create or update, so the parent can reload the list. */
  readonly saved = output<void>();
  /** Emitted when the user cancels an in-progress edit. */
  readonly cancelled = output<void>();

  /** Rating choices offered in the dropdown. */
  protected readonly ratingOptions = [1, 2, 3, 4, 5];

  /** Server-side (RFC 7807) field errors, keyed by field name. */
  private readonly fieldErrors = signal<Record<string, string>>({});

  /** True when the form is editing an existing review rather than creating one. */
  protected readonly isEditMode = computed(() => this.reviewToEdit() !== null);

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(ReviewForm.MAX_TITLE_LENGTH)]],
    comment: ['', [Validators.maxLength(ReviewForm.MAX_COMMENT_LENGTH)]],
    rating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
  });

  constructor() {
    // Keep the form in sync with the edit target: prefill on edit, reset on add.
    // emitEvent:false avoids kicking off validation events during programmatic patching.
    effect(() => {
      const review = this.reviewToEdit();
      if (review) {
        this.form.patchValue(
          { title: review.title ?? '', comment: review.comment ?? '', rating: review.rating ?? 5 },
          { emitEvent: false },
        );
      } else {
        this.form.reset({ title: '', comment: '', rating: 5 }, { emitEvent: false });
      }
      this.fieldErrors.set({});
    });
  }

  /** Whether a field should render as invalid (client validation or a server error). */
  protected isInvalid(field: 'title' | 'comment' | 'rating'): boolean {
    const control = this.form.get(field);
    const clientInvalid = !!control && control.invalid && (control.touched || control.dirty);
    return clientInvalid || !!this.fieldErrors()[field];
  }

  /** The message to display for an invalid field (server error takes priority). */
  protected errorFor(field: 'title' | 'comment' | 'rating'): string {
    const serverMsg = this.fieldErrors()[field];
    if (serverMsg) return serverMsg;

    const control = this.form.get(field);
    if (control?.hasError('required')) return 'This field is required.';
    if (control?.hasError('maxlength')) return 'This value is too long.';
    if (control?.hasError('min') || control?.hasError('max')) return 'Rating must be between 1 and 5.';
    return 'Invalid value.';
  }

  /** Submits the form as a create or an update depending on mode. */
  protected onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.fieldErrors.set({});
    const { title, comment, rating } = this.form.getRawValue();
    const editing = this.reviewToEdit();

    const request$ = editing
      ? this.reviewService.updateReview(editing.id!, { title, comment, rating })
      : this.reviewService.addReview(this.targetId(), this.targetType(), { title, comment, rating });

    request$.subscribe({
      next: () => {
        this.toastService.showSuccess(editing ? 'Review updated.' : 'Review added.');
        this.form.reset({ title: '', comment: '', rating: 5 });
        this.saved.emit();
      },
      error: (err: HttpErrorResponse) => this.handleError(err),
    });
  }

  /** Cancels an in-progress edit. */
  protected onCancel(): void {
    this.cancelled.emit();
  }

  /**
   * Maps an error response to either per-field messages (RFC 7807 validation `errors` map)
   * or a general toast for anything else (e.g. a 409 duplicate, or a network failure).
   */
  private handleError(err: HttpErrorResponse): void {
    const errors = err.error?.errors as Record<string, string> | undefined;
    if (errors && Object.keys(errors).length > 0) {
      this.fieldErrors.set(errors);
    } else {
      this.toastService.showError(err.error?.detail ?? 'Failed to save the review.');
    }
  }
}
