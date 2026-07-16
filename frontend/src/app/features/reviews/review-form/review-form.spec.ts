import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi, describe, it, expect, beforeEach, afterEach, Mock } from 'vitest';
import { HttpErrorResponse } from '@angular/common/http';
import { ReviewForm } from './review-form';
import { ReviewService } from '../../../core/services/review.service';
import { ToastService } from '../../../core/services/toast.service';
import { ReviewViewDto } from '../../../api';

describe('ReviewForm', () => {
  let fixture: ComponentFixture<ReviewForm>;
  let reviewService: { addReview: Mock; updateReview: Mock };
  let toastService: { showSuccess: Mock; showError: Mock };

  const existingReview: ReviewViewDto = {
    id: 'review-1',
    title: 'Original title',
    comment: 'Original comment',
    rating: 4,
    userId: 'user-1',
    username: 'alice',
    targetId: 'book-1',
    targetType: 'BOOK',
    createdAt: '2026-07-13T10:00:00Z',
    updatedAt: '2026-07-13T10:00:00Z',
  };

  // --- DOM helpers ---
  const setInput = (selector: string, value: string): void => {
    const el = fixture.nativeElement.querySelector(selector) as HTMLInputElement | HTMLTextAreaElement;
    el.value = value;
    el.dispatchEvent(new Event('input'));
  };
  const submit = (): void => {
    (fixture.nativeElement.querySelector('form') as HTMLFormElement)
      .dispatchEvent(new Event('submit'));
  };

  const hasInvalidTitle = (): boolean =>
    !!fixture.nativeElement.querySelector('#review-title.is-invalid');

  async function init(reviewToEdit: ReviewViewDto | null = null): Promise<void> {
    fixture = TestBed.createComponent(ReviewForm);
    fixture.componentRef.setInput('targetId', 'book-1');
    if (reviewToEdit) {
      fixture.componentRef.setInput('reviewToEdit', reviewToEdit);
    }
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(() => {
    reviewService = {
      addReview: vi.fn().mockReturnValue(of(existingReview)),
      updateReview: vi.fn().mockReturnValue(of(existingReview)),
    };
    toastService = {
      showSuccess: vi.fn(),
      showError: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [ReviewForm],
      providers: [
        { provide: ReviewService, useValue: reviewService },
        { provide: ToastService, useValue: toastService },
      ],
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('add mode', () => {
    it('creates a review and emits saved on valid submit', async () => {
      await init();
      const savedSpy = vi.spyOn(fixture.componentInstance.saved, 'emit');

      setInput('#review-title', 'Fantastic');
      setInput('#review-comment', 'Really enjoyed it');
      // rating defaults to 5
      submit();
      await fixture.whenStable();

      expect(reviewService.addReview).toHaveBeenCalledWith('book-1', 'BOOK', {
        title: 'Fantastic',
        comment: 'Really enjoyed it',
        rating: 5,
      });
      expect(reviewService.updateReview).not.toHaveBeenCalled();
      expect(toastService.showSuccess).toHaveBeenCalledWith('Review added.');
      expect(savedSpy).toHaveBeenCalledTimes(1);
    });

    it('does not submit when the title is missing', async () => {
      await init();
      // leave title empty
      submit();
      await fixture.whenStable();

      expect(reviewService.addReview).not.toHaveBeenCalled();
      expect(hasInvalidTitle()).toBe(true);
    });

    it('surfaces server-side field errors (RFC 7807) without a toast', async () => {
      reviewService.addReview.mockReturnValue(
        throwError(() => new HttpErrorResponse({
          status: 400,
          error: { errors: { title: 'Title already used' } },
        })),
      );
      await init();

      setInput('#review-title', 'Dup');
      submit();
      await fixture.whenStable();
      fixture.detectChanges();

      expect(hasInvalidTitle()).toBe(true);
      expect(toastService.showError).not.toHaveBeenCalled();
    });

    it('shows a toast for a non-field error (e.g. duplicate 409)', async () => {
      reviewService.addReview.mockReturnValue(
        throwError(() => new HttpErrorResponse({
          status: 409,
          error: { detail: 'You have already reviewed this item.' },
        })),
      );
      await init();

      setInput('#review-title', 'Second attempt');
      submit();
      await fixture.whenStable();

      expect(toastService.showError).toHaveBeenCalledWith('You have already reviewed this item.');
    });

    it('emits cancelled when Cancel is clicked in add mode', async () => {
      // Arrange: add mode (no reviewToEdit)
      await init();
      const cancelledSpy = vi.spyOn(fixture.componentInstance.cancelled, 'emit');

      // Act
      const cancelBtn = fixture.nativeElement.querySelector('.btn-outline-secondary') as HTMLElement;
      cancelBtn.click();

      // Assert
      expect(cancelledSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe('edit mode', () => {
    it('prefills from the review and calls update on submit', async () => {
      await init(existingReview);
      const savedSpy = vi.spyOn(fixture.componentInstance.saved, 'emit');

      const titleEl = fixture.nativeElement.querySelector('#review-title') as HTMLInputElement;
      expect(titleEl.value).toBe('Original title'); // prefilled by the effect

      setInput('#review-title', 'Updated title');
      submit();
      await fixture.whenStable();

      expect(reviewService.updateReview).toHaveBeenCalledWith('review-1', {
        title: 'Updated title',
        comment: 'Original comment',
        rating: 4,
      });
      expect(reviewService.addReview).not.toHaveBeenCalled();
      expect(toastService.showSuccess).toHaveBeenCalledWith('Review updated.');
      expect(savedSpy).toHaveBeenCalledTimes(1);
    });

    it('emits cancelled when Cancel is clicked', async () => {
      await init(existingReview);
      const cancelledSpy = vi.spyOn(fixture.componentInstance.cancelled, 'emit');

      const cancelBtn = fixture.nativeElement.querySelector('.btn-outline-secondary') as HTMLElement;
      cancelBtn.click();

      expect(cancelledSpy).toHaveBeenCalledTimes(1);
    });
  });
});
