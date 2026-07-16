import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach, Mock } from 'vitest';

import { ReviewList } from './review-list';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ReviewViewDto, PagedResponseReviewViewDto } from '../../../api';

describe('ReviewList', () => {
  let fixture: ComponentFixture<ReviewList>;

  let reviewService: { getReviewsForTarget: Mock; deleteReview: Mock };
  let authService: { userId: Mock; userRole: Mock };
  let toastService: { showSuccess: Mock; showError: Mock };

  // --- Test data factories ---

  const aReview = (overrides: Partial<ReviewViewDto> = {}): ReviewViewDto => ({
    id: 'review-1',
    title: 'Great read',
    comment: 'Loved every page.',
    rating: 5,
    userId: 'user-1',
    username: 'alice',
    targetId: 'book-1',
    targetType: 'BOOK',
    createdAt: '2026-07-13T10:00:00Z',
    updatedAt: '2026-07-13T10:00:00Z',
    ...overrides,
  });

  const aPage = (
    content: ReviewViewDto[],
    overrides: Partial<PagedResponseReviewViewDto> = {},
  ): PagedResponseReviewViewDto => ({
    content,
    pageNumber: 0,
    pageSize: 5,
    totalElements: content.length,
    totalPages: 1,
    isLast: true,
    ...overrides,
  });

  // --- Typed harness: selectors and interactions in one place ---

  const editButton = (): DebugElement | null =>
    fixture.debugElement.query(By.css('.btn-outline-secondary'));

  const deleteButton = (): DebugElement | null =>
    fixture.debugElement.query(By.css('.btn-outline-danger'));

  const paginationButtons = (): DebugElement[] =>
    fixture.debugElement.queryAll(By.css('.btn-outline-primary'));

  const pageText = (): string => fixture.nativeElement.textContent ?? '';

  const click = (el: DebugElement): void => el.triggerEventHandler('click', null);

  /** Creates the component, sets required inputs, and flushes the async data load. */
  async function init(): Promise<void> {
    fixture = TestBed.createComponent(ReviewList);
    fixture.componentRef.setInput('targetId', 'book-1');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    reviewService = {
      getReviewsForTarget: vi.fn().mockReturnValue(of(aPage([aReview()]))),
      deleteReview: vi.fn().mockReturnValue(of(undefined)),
    };
    authService = {
      userId: vi.fn().mockReturnValue('user-1'),
      userRole: vi.fn().mockReturnValue('ROLE_USER'),
    };
    toastService = {
      showSuccess: vi.fn(),
      showError: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ReviewList],
      providers: [
        { provide: ReviewService, useValue: reviewService },
        { provide: AuthService, useValue: authService },
        { provide: ToastService, useValue: toastService },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('loading and rendering', () => {
    it('requests reviews for the target on init', async () => {
      await init();
      expect(reviewService.getReviewsForTarget).toHaveBeenCalledWith('book-1', 'BOOK', { page: 0, size: 5 });
    });

    it('renders a review with its details', async () => {
      await init();
      const title = fixture.debugElement.query(By.css('.card-title'));
      expect(title.nativeElement.textContent).toContain('Great read');
      expect(pageText()).toContain('alice');
      expect(pageText()).toContain('Loved every page.');
      expect(pageText()).toContain('5/5');
    });

    it('shows an empty state when there are no reviews', async () => {
      reviewService.getReviewsForTarget.mockReturnValue(of(aPage([])));
      await init();
      expect(pageText()).toContain('No reviews yet');
    });
  });

  // Permission logic is verified through its only observable effect: which
  // action buttons the template renders. This asserts behaviour, not internals.
  describe('permission-driven affordances', () => {
    it('offers edit and delete to the review author', async () => {
      authService.userId.mockReturnValue('user-1'); // author of the default review
      await init();
      expect(editButton()).not.toBeNull();
      expect(deleteButton()).not.toBeNull();
    });

    it('offers delete but not edit to an admin viewing another user\'s review', async () => {
      reviewService.getReviewsForTarget.mockReturnValue(of(aPage([aReview({ userId: 'someone-else' })])));
      authService.userId.mockReturnValue('admin-id');
      authService.userRole.mockReturnValue('ROLE_ADMIN');
      await init();
      expect(editButton()).toBeNull();
      expect(deleteButton()).not.toBeNull();
    });

    it('offers neither edit nor delete to a non-author, non-admin', async () => {
      reviewService.getReviewsForTarget.mockReturnValue(of(aPage([aReview({ userId: 'someone-else' })])));
      authService.userId.mockReturnValue('user-2');
      authService.userRole.mockReturnValue('ROLE_USER');
      await init();
      expect(editButton()).toBeNull();
      expect(deleteButton()).toBeNull();
    });

    it('offers neither edit nor delete to a guest', async () => {
      authService.userId.mockReturnValue(null);
      authService.userRole.mockReturnValue(null);
      await init();
      expect(editButton()).toBeNull();
      expect(deleteButton()).toBeNull();
    });
  });

  describe('deleting a review', () => {
    it('deletes, confirms, and reloads the list', async () => {
      await init();
      expect(reviewService.getReviewsForTarget).toHaveBeenCalledTimes(1);

      click(deleteButton()!);
      await fixture.whenStable();

      expect(reviewService.deleteReview).toHaveBeenCalledWith('review-1');
      expect(toastService.showSuccess).toHaveBeenCalledWith('Review deleted.');
      expect(reviewService.getReviewsForTarget).toHaveBeenCalledTimes(2); // reloaded
    });

    it('surfaces the error and does not reload when deletion fails', async () => {
      reviewService.deleteReview.mockReturnValue(
        throwError(() => ({ error: { detail: 'Cannot delete' } })),
      );
      await init();

      click(deleteButton()!);
      await fixture.whenStable();

      expect(toastService.showError).toHaveBeenCalledWith('Cannot delete');
      expect(reviewService.getReviewsForTarget).toHaveBeenCalledTimes(1); // no reload
    });
  });

  describe('editing a review', () => {
    it('emits editRequested with the review when edit is chosen', async () => {
      await init();
      const emitSpy = vi.spyOn(fixture.componentInstance.editRequested, 'emit');

      click(editButton()!);

      expect(emitSpy).toHaveBeenCalledTimes(1);
      expect(emitSpy).toHaveBeenCalledWith(expect.objectContaining({ id: 'review-1' }));
    });
  });

  describe('pagination', () => {
    it('loads the next page when Next is chosen', async () => {
      reviewService.getReviewsForTarget.mockReturnValue(of(aPage([aReview()], { totalPages: 2, isLast: false })));
      await init();

      // [0] Previous (disabled on page 0), [1] Next
      click(paginationButtons()[1]);
      await fixture.whenStable();

      expect(reviewService.getReviewsForTarget).toHaveBeenCalledWith('book-1', 'BOOK', { page: 1, size: 5 });
    });
  });
});
