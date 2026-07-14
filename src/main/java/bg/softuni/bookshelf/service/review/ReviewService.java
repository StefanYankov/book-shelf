package bg.softuni.bookshelf.service.review;

import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewUpdateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for managing reviews.
 */
public interface ReviewService {

    /**
     * Retrieves a paginated list of reviews for a specific target.
     *
     * @param targetId   The ID of the target entity.
     * @param targetType The type of the target entity.
     * @param pageable   The pagination information.
     * @return A page of review view DTOs.
     */
    Page<ReviewViewDto> getReviewsForTarget(UUID targetId, String targetType, Pageable pageable);

    /**
     * Adds a new review for a target entity.
     *
     * @param createDto  The DTO containing the review data.
     * @param targetId   The ID of the target entity.
     * @param targetType The type of the target entity.
     * @param userId     The ID of the user creating the review.
     * @return The created review as a view DTO.
     * @throws BusinessException if the user has already reviewed this target.
     */
    ReviewViewDto addReview(ReviewCreateDto createDto, UUID targetId, String targetType, UUID userId);

    /**
     * Updates an existing review.
     *
     * @param reviewId  The ID of the review to update.
     * @param updateDto The DTO containing the updated data.
     * @param userId    The ID of the user performing the update.
     * @return The updated review as a view DTO.
     * @throws BusinessException if the review is not found or the user is not authorized to modify it.
     */
    ReviewViewDto updateReview(UUID reviewId, ReviewUpdateDto updateDto, UUID userId);

    /**
     * Deletes a review. Permitted for the review's author or an administrator.
     *
     * @param reviewId The ID of the review to delete.
     * @param userId   The ID of the user performing the deletion.
     * @param isAdmin  Whether the caller holds administrative privileges.
     * @throws BusinessException if the review is not found or the user is not authorized.
     */
    void deleteReview(UUID reviewId, UUID userId, boolean isAdmin);
}