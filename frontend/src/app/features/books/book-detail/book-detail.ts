import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { switchMap, of } from 'rxjs';
import { ReviewList } from '../../reviews/review-list/review-list';
import { ReviewForm } from '../../reviews/review-form/review-form';
import { BookAPIService, UserShelfAPIService } from '../../../api';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { AddBookToBookshelfDto, PagedResponseBookshelfSummaryDto, ReviewViewDto } from '../../../api';

@Component({
  selector: 'app-book-detail',
  imports: [CommonModule, ReviewList, ReviewForm],
  templateUrl: './book-detail.html',
  styleUrls: ['./book-detail.css'],
})
export class BookDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly bookApiService = inject(BookAPIService);
  private readonly userShelfApiService = inject(UserShelfAPIService);
  private readonly toastService = inject(ToastService);
  public readonly authService = inject(AuthService);

  /** Reference to the embedded review list, used to trigger a reload after a save. */
  private readonly reviewList = viewChild(ReviewList);

  book = toSignal(
    this.route.paramMap.pipe(
      switchMap(params => this.bookApiService.getBookById(params.get('id')!))
    )
  );

  // --- User Shelves State ---
  userShelves = toSignal(
    this.authService.isLoggedIn()
      ? this.userShelfApiService.getUserShelves({ page: 0, size: 100 })
      : of({ content: [], totalPages: 0, pageNumber: 0, totalElements: 0, pageSize: 20, isLast: true } as PagedResponseBookshelfSummaryDto)
  );

  // --- Review coordination state ---

  /** The review currently being edited, or null when adding / not editing. */
  protected readonly editingReview = signal<ReviewViewDto | null>(null);

  /** Whether the add-review form is expanded (vs. showing the "Write a review" button). */
  protected readonly addFormOpen = signal(false);

  /** Reviews currently loaded by the child list (used to detect the user's own review). */
  private readonly loadedReviews = signal<ReviewViewDto[]>([]);

  /** The current user's own review among those loaded, or null if none. */
  protected readonly myReview = computed<ReviewViewDto | null>(() => {
    const uid = this.authService.userId();
    if (!uid) return null;
    return this.loadedReviews().find(r => r.userId === uid) ?? null;
  });

  /**
   * Whether the "Write a review" affordance should be available: the user is logged in,
   * is not currently editing, and has not already reviewed this book (one-per-book).
   */
  protected readonly showAddForm = computed(() =>
    this.authService.isLoggedIn() && this.editingReview() === null && this.myReview() === null
  );

  /** Captures the reviews the list has loaded so we can detect the user's own review. */
  protected onReviewsLoaded(reviews: ReviewViewDto[]): void {
    this.loadedReviews.set(reviews);
  }

  /** Opens the add-review form. */
  protected openAddForm(): void {
    this.addFormOpen.set(true);
  }

  /** Cancels the add-review form. */
  protected onAddCancelled(): void {
    this.addFormOpen.set(false);
  }

  /** Switches the form into edit mode for the chosen review. */
  protected onEditRequested(review: ReviewViewDto): void {
    this.editingReview.set(review);
  }

  /** Cancels an in-progress edit without saving. */
  protected onEditCancelled(): void {
    this.editingReview.set(null);
  }

  /** After a successful add/edit: exit both edit and add modes, and refresh the list. */
  protected onReviewSaved(): void {
    this.editingReview.set(null);
    this.addFormOpen.set(false);
    this.reviewList()?.reload();
  }

  /**
   * Adds a book to the selected shelf.
   * @param bookId The UUID of the book to add.
   * @param shelfId The UUID of the target shelf.
   */
  addToShelf(bookId: string, shelfId: string) {
    if (!bookId || !shelfId) return;

    const addBookDto: AddBookToBookshelfDto = { bookId };
    this.userShelfApiService.addBookToShelf(shelfId, addBookDto).subscribe({
      next: () => {
        this.toastService.showSuccess('Book added to shelf successfully!');
      },
      error: (err) => {
        this.toastService.showError(err.error?.detail || 'Failed to add book to shelf.');
      }
    });
  }
}
