import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {
  Pageable,
  PagedResponseReviewViewDto,
  ReviewAPIService,
  ReviewCreateDto,
  ReviewUpdateDto,
  ReviewViewDto,
} from '../../api';

/**
 * Review target types supported by the backend's polymorphic association.
 * Currently only BOOK; AUTHOR/PUBLISHER will be added when those pages exist.
 */
export type ReviewTargetType = 'BOOK';

/**
 * Service responsible for managing reviews.
 * Acts as a facade over the generated OpenAPI client (`ReviewAPIService`),
 * hiding the polymorphic (targetId, targetType) contract behind domain-named methods.
 */
@Injectable({providedIn: 'root'})
export class ReviewService {
  private readonly reviewApiService = inject(ReviewAPIService);

  /**
   * Retrieves a paginated list of reviews for a given target.
   * @param targetId The UUID of the target entity (e.g. a book).
   * @param targetType The kind of target being reviewed.
   * @param pageable Pagination and sorting configuration.
   * @returns An observable emitting a paginated result of reviews.
   */
  getReviewsForTarget(
    targetId: string,
    targetType: ReviewTargetType,
    pageable: Pageable,
  ): Observable<PagedResponseReviewViewDto> {
    return this.reviewApiService.getReviewsForTarget(targetId, targetType, pageable);
  }

  /**
   * Creates a new review for a target. A user may only review a given target once.
   * @param targetId The UUID of the target entity.
   * @param targetType The kind of target being reviewed.
   * @param createDto The review payload (title, comment, rating).
   * @returns An observable emitting the newly created review.
   */
  addReview(
    targetId: string,
    targetType: ReviewTargetType,
    createDto: ReviewCreateDto,
  ): Observable<ReviewViewDto> {
    return this.reviewApiService.addReview(targetId, targetType, createDto);
  }

  /**
   * Updates an existing review. Permitted only for the review's author.
   * @param reviewId The UUID of the review to update.
   * @param updateDto The updated review payload.
   * @returns An observable emitting the updated review.
   */
  updateReview(reviewId: string, updateDto: ReviewUpdateDto): Observable<ReviewViewDto> {
    return this.reviewApiService.updateReview(reviewId, updateDto);
  }

  /**
   * Deletes a review. Permitted for the review's author or an administrator
   * (authorization is enforced server-side).
   * @param reviewId The UUID of the review to delete.
   * @returns An observable that completes when the deletion succeeds.
   */
  deleteReview(reviewId: string): Observable<void> {
    return this.reviewApiService.deleteReview(reviewId);
  }
}
