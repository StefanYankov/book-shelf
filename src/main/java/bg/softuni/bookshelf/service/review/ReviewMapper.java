package bg.softuni.bookshelf.service.review;

import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper component for converting between Review DTOs and entities.
 */
@Component
public class ReviewMapper {

    /**
     * Maps a ReviewCreateDto to a new Review entity.
     *
     * @param dto        The source DTO.
     * @param userId     The ID of the user creating the review.
     * @param targetId   The ID of the entity being reviewed.
     * @param targetType The type of the entity being reviewed.
     * @return A new {@link Review} entity.
     */
    public Review toReviewEntity(ReviewCreateDto dto, UUID userId, UUID targetId, String targetType) {
        Review review = new Review();
        review.setUserId(userId);
        review.setTargetId(targetId);
        review.setTargetType(targetType);
        review.setTitle(dto.title());
        review.setComment(dto.comment());
        review.setRating(dto.rating());
        return review;
    }

    /**
     * Maps a Review entity to a ReviewViewDto.
     *
     * @param review   The source entity.
     * @param username The username of the user who created the review.
     * @return A {@link ReviewViewDto}.
     */
    public ReviewViewDto toReviewViewDto(Review review, String username) {
        return new ReviewViewDto(
                review.getId(),
                review.getTitle(),
                review.getComment(),
                review.getRating(),
                review.getUserId(),
                username,
                review.getTargetId(),
                review.getTargetType(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
