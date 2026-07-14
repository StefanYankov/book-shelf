package bg.softuni.bookshelf.service.review;

import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.repository.ReviewRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.base.BaseService;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewUpdateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends BaseService implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewViewDto> getReviewsForTarget(UUID targetId, String targetType, Pageable pageable) {
        log.debug("Fetching reviews for target type {} with ID {}", targetType, targetId);
        Objects.requireNonNull(targetId, DeveloperErrors.ENTITY_ID_NULL);
        Objects.requireNonNull(targetType, DeveloperErrors.NAME_NULL);
        Objects.requireNonNull(pageable, DeveloperErrors.PAGEABLE_NULL);

        Page<Review> reviews = reviewRepository.findAllByTargetIdAndTargetType(targetId, targetType, pageable);

        Set<UUID> authorIds = reviews.stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());

        Map<UUID, String> usernamesById = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return reviews.map(review ->
                reviewMapper.toReviewViewDto(
                        review,
                        // Fallback keeps a listing endpoint resilient if an author was deleted
                        // (userId is a soft UUID reference, not an FK — so this is possible).
                        usernamesById.getOrDefault(review.getUserId(), "[deleted user]")
                ));
    }

    @Override
    @Transactional
    public ReviewViewDto addReview(ReviewCreateDto createDto, UUID targetId, String targetType, UUID userId) {
        log.debug("Attempting to add review for target type {} with ID {} by user {}", targetType, targetId, userId);
        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        Objects.requireNonNull(targetId, DeveloperErrors.ENTITY_ID_NULL);
        Objects.requireNonNull(userId, DeveloperErrors.ENTITY_ID_NULL);

        if (reviewRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)) {
            log.warn("Duplicate review attempt rejected for user {} on target {}", userId, targetId);
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        Review review = reviewMapper.toReviewEntity(createDto, userId, targetId, targetType);

        Review savedReview = reviewRepository.save(review);
        log.info("Successfully created review {} for target {}", savedReview.getId(), targetId);

        return reviewMapper.toReviewViewDto(savedReview, user.getUsername());
    }

    @Override
    @Transactional
    public ReviewViewDto updateReview(UUID reviewId, ReviewUpdateDto updateDto, UUID userId) {
        log.debug("Attempting to update review {} by user {}", reviewId, userId);
        Objects.requireNonNull(reviewId, DeveloperErrors.ENTITY_ID_NULL);
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);
        Objects.requireNonNull(userId, DeveloperErrors.ENTITY_ID_NULL);

        Review review = findOrThrow(() -> reviewRepository.findById(reviewId), ErrorCode.REVIEW_NOT_FOUND, reviewId);

        if (!review.getUserId().equals(userId)) {
            log.warn("Unauthorized modification attempt. User {} tried to alter review {} owned by {}", userId, reviewId, review.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION);
        }

        review.setTitle(updateDto.title());
        review.setComment(updateDto.comment());
        review.setRating(updateDto.rating());

        Review updatedReview = reviewRepository.save(review);
        log.info("Successfully updated review {}", updatedReview.getId());

        User user = findOrThrow(() -> userRepository.findById(userId), ErrorCode.USER_NOT_FOUND, userId);
        return reviewMapper.toReviewViewDto(updatedReview, user.getUsername());
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId, boolean isAdmin) {
        log.debug("Attempting to delete review {} by user {}", reviewId, userId);
        Objects.requireNonNull(reviewId, DeveloperErrors.ENTITY_ID_NULL);
        Objects.requireNonNull(userId, DeveloperErrors.ENTITY_ID_NULL);

        Review review = findOrThrow(() -> reviewRepository.findById(reviewId), ErrorCode.REVIEW_NOT_FOUND, reviewId);

        if (!review.getUserId().equals(userId) && !isAdmin) {
            log.warn("Unauthorized deletion attempt. User {} lacks ownership or ADMIN privileges for review {}", userId, reviewId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION);
        }

        reviewRepository.delete(review);
        log.info("Successfully deleted review {}", reviewId);
    }
}