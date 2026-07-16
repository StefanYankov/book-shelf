import { TestBed } from '@angular/core/testing';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { of } from 'rxjs';
import { ReviewService } from './review.service';
import { ReviewAPIService, ReviewCreateDto, ReviewUpdateDto } from '../../api';

describe('ReviewService', () => {
  let service: ReviewService;
  let mockReviewApiService: {
    getReviewsForTarget: ReturnType<typeof vi.fn>;
    addReview: ReturnType<typeof vi.fn>;
    updateReview: ReturnType<typeof vi.fn>;
    deleteReview: ReturnType<typeof vi.fn>;
  };

  const targetId = 'book-123';
  const reviewId = 'review-456';

  beforeEach(() => {
    mockReviewApiService = {
      getReviewsForTarget: vi.fn().mockReturnValue(of({})),
      addReview: vi.fn().mockReturnValue(of({})),
      updateReview: vi.fn().mockReturnValue(of({})),
      deleteReview: vi.fn().mockReturnValue(of(undefined)),
    };

    TestBed.configureTestingModule({
      providers: [
        ReviewService,
        { provide: ReviewAPIService, useValue: mockReviewApiService },
      ],
    });
    service = TestBed.inject(ReviewService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getReviewsForTarget', () => {
    it('should delegate to the API client with target and pageable', () => {
      const pageable = { page: 0, size: 20 };

      service.getReviewsForTarget(targetId, 'BOOK', pageable);

      expect(mockReviewApiService.getReviewsForTarget).toHaveBeenCalledWith(targetId, 'BOOK', pageable);
    });
  });

  describe('addReview', () => {
    it('should delegate to the API client with target and payload', () => {
      const dto: ReviewCreateDto = { title: 'Great', comment: 'Loved it', rating: 5 };

      service.addReview(targetId, 'BOOK', dto);

      expect(mockReviewApiService.addReview).toHaveBeenCalledWith(targetId, 'BOOK', dto);
    });
  });

  describe('updateReview', () => {
    it('should delegate to the API client with review id and payload', () => {
      const dto: ReviewUpdateDto = { title: 'Updated', comment: 'Still good', rating: 4 };

      service.updateReview(reviewId, dto);

      expect(mockReviewApiService.updateReview).toHaveBeenCalledWith(reviewId, dto);
    });
  });

  describe('deleteReview', () => {
    it('should delegate to the API client with the review id', () => {
      service.deleteReview(reviewId);

      expect(mockReviewApiService.deleteReview).toHaveBeenCalledWith(reviewId);
    });
  });
});
