import { Component, inject, input, output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { switchMap, tap } from 'rxjs/operators';
import {ReviewService, ReviewTargetType} from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ReviewViewDto } from '../../../api';

/**
 * Displays a paginated list of reviews for a given target (e.g. a book).
 * Read access is public; per-row edit/delete affordances are gated by ownership
 * (author) and role (admin). Editing is delegated to a parent via editRequested;
 * deletion is handled here and reloads the page.
 */
@Component({
  selector: 'app-review-list',
  imports: [CommonModule],
  templateUrl: './review-list.html',
  styleUrls: ['./review-list.css'],
})
export class ReviewList {
  private readonly reviewService = inject(ReviewService);
  protected readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  private static readonly PAGE_SIZE = 5;

  /** The target entity being reviewed (e.g. a book id). Required. */
  readonly targetId = input.required<string>();
  /** The kind of target; defaults to BOOK so the component can be reused later. */
  readonly targetType = input<ReviewTargetType>('BOOK');

  /** Emitted when the user asks to edit one of their own reviews. */
  readonly editRequested = output<ReviewViewDto>();

  /** Emits the reviews on the current page whenever they load, so a parent can
   *  derive state such as "has the current user already reviewed this target?". */
  readonly reviewsLoaded = output<ReviewViewDto[]>();

  /** Current zero-based page index. */
  private readonly page = signal(0);

  /** Bumped to force a reload after a mutation. */
  private readonly reloadTrigger = signal(0);

  /** Aggregated request key; recomputes on target/page/reload change. */
  private readonly query = computed(() => ({
    targetId: this.targetId(),
    targetType: this.targetType(),
    page: this.page(),
    reload: this.reloadTrigger(),
  }));

  /** Declarative paginated reviews. `undefined` while loading. */
  protected readonly reviewsPage = toSignal(
    toObservable(this.query).pipe(
      switchMap(q =>
        this.reviewService.getReviewsForTarget(q.targetId, q.targetType, {
          page: q.page,
          size: ReviewList.PAGE_SIZE,
        })
      ),
      tap(page => this.reviewsLoaded.emit(page.content ?? [])),
    )
  );

  protected canEdit(review: ReviewViewDto): boolean {
    return review.userId != null && review.userId === this.authService.userId();
  }

  protected canDelete(review: ReviewViewDto): boolean {
    return this.canEdit(review) || this.authService.userRole() === 'ROLE_ADMIN';
  }

  protected onEdit(review: ReviewViewDto): void {
    this.editRequested.emit(review);
  }

  protected onDelete(reviewId: string): void {
    this.reviewService.deleteReview(reviewId).subscribe({
      next: () => {
        this.toastService.showSuccess('Review deleted.');
        this.reload();
      },
      error: err => this.toastService.showError(err.error?.detail || 'Failed to delete review.'),
    });
  }

  protected goToPage(page: number): void {
    this.page.set(page);
  }

  /** Forces a reload of the current page. Public so the parent can refresh after add/edit. */
  reload(): void {
    this.reloadTrigger.update(v => v + 1);
  }
}
