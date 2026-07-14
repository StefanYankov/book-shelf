package bg.softuni.bookshelf.service.review;

import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.repository.ReviewRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewUpdateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    // --- TEST DATA FACTORY ---
    private final UUID targetId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final String targetType = "BOOK";

    private ReviewCreateDto createValidCreateDto() {
        return new ReviewCreateDto("Masterpiece", "Loved it", 5);
    }

    private ReviewUpdateDto createValidUpdateDto() {
        return new ReviewUpdateDto("Updated Title", "Updated Comment", 4);
    }

    private ApplicationUser createMockUser() {
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        user.setUsername("testuser");
        return user;
    }

    @Nested
    @DisplayName("getReviewsForTarget(UUID, String, Pageable) Tests")
    class GetReviewsTests {

        @Test
        @DisplayName("Defense in Depth: Should throw exception on null targetId")
        void shouldThrowOnNullTargetId() {
            assertThatThrownBy(() -> reviewService.getReviewsForTarget(null, targetType, PageRequest.of(0, 10)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Happy Path: Should return mapped paginated results")
        void shouldReturnPaginatedResults() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Review mockReview = new Review();
            mockReview.setUserId(userId);
            Page<Review> reviewPage = new PageImpl<>(List.of(mockReview));
            ApplicationUser mockUser = createMockUser();
            ReviewViewDto expectedDto = new ReviewViewDto(UUID.randomUUID(), "T", "C", 5, userId, "testuser", targetId, targetType, Instant.now(), Instant.now());

            given(reviewRepository.findAllByTargetIdAndTargetType(targetId, targetType, pageable)).willReturn(reviewPage);
            // Batched author lookup (N+1 fix) — one call for all authors, not one per review.
            given(userRepository.findAllById(any())).willReturn(List.of(mockUser));
            given(reviewMapper.toReviewViewDto(mockReview, "testuser")).willReturn(expectedDto);

            // Act
            Page<ReviewViewDto> result = reviewService.getReviewsForTarget(targetId, targetType, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(expectedDto);
        }
    }

    @Nested
    @DisplayName("addReview(...) Tests")
    class AddReviewTests {

        @Test
        @DisplayName("Defense in Depth: Should throw exception on null DTO")
        void shouldThrowOnNullDto() {
            assertThatThrownBy(() -> reviewService.addReview(null, targetId, targetType, userId))
                    .isInstanceOf(NullPointerException.class);
            verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when review already exists")
        void shouldThrowWhenDuplicateExists() {
            // Arrange
            ReviewCreateDto dto = createValidCreateDto();
            given(reviewRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> reviewService.addReview(dto, targetId, targetType, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_REVIEW);

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Happy Path: Should save and map successfully")
        void shouldSaveReviewSuccessfully() {
            // Arrange
            ReviewCreateDto dto = createValidCreateDto();
            ApplicationUser mockUser = createMockUser();
            Review mappedEntity = new Review();
            Review savedEntity = new Review();
            ReviewViewDto expectedDto = new ReviewViewDto(UUID.randomUUID(), "T", "C", 5, userId, "testuser", targetId, targetType, Instant.now(), Instant.now());

            given(reviewRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(reviewMapper.toReviewEntity(dto, userId, targetId, targetType)).willReturn(mappedEntity);
            given(reviewRepository.save(mappedEntity)).willReturn(savedEntity);
            given(reviewMapper.toReviewViewDto(savedEntity, "testuser")).willReturn(expectedDto);

            // Act
            ReviewViewDto result = reviewService.addReview(dto, targetId, targetType, userId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            verify(reviewRepository).save(mappedEntity);
        }
    }

    @Nested
    @DisplayName("updateReview(UUID, ReviewUpdateDto, UUID) Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Error Case: Should throw BusinessException if user is not author")
        void shouldThrowIfUnauthorized() {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            UUID hackerId = UUID.randomUUID();
            ReviewUpdateDto dto = createValidUpdateDto();

            Review existingReview = new Review();
            existingReview.setUserId(userId); // Belongs to original user

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // Act & Assert
            assertThatThrownBy(() -> reviewService.updateReview(reviewId, dto, hackerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION);

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Happy Path: Should update fields and save")
        void shouldUpdateReviewSuccessfully() {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateDto dto = createValidUpdateDto();
            ApplicationUser mockUser = createMockUser();
            ReviewViewDto expectedDto = new ReviewViewDto(UUID.randomUUID(), "T", "C", 5, userId, "testuser", targetId, targetType, Instant.now(), Instant.now());

            Review existingReview = new Review();
            existingReview.setUserId(userId);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));
            given(reviewRepository.save(any())).willReturn(existingReview);
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
            given(reviewMapper.toReviewViewDto(existingReview, "testuser")).willReturn(expectedDto);

            // Act
            ReviewViewDto result = reviewService.updateReview(reviewId, dto, userId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            verify(reviewRepository).save(reviewCaptor.capture());
            Review captured = reviewCaptor.getValue();
            assertThat(captured.getTitle()).isEqualTo("Updated Title");
            assertThat(captured.getComment()).isEqualTo("Updated Comment");
            assertThat(captured.getRating()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("deleteReview(UUID, UUID, boolean) Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Error Case: Should throw BusinessException if caller is neither author nor admin")
        void shouldThrowIfUnauthorized() {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            UUID hackerId = UUID.randomUUID();

            Review existingReview = new Review();
            existingReview.setUserId(userId); // Belongs to original user

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // Act & Assert: non-owner, non-admin (isAdmin=false)
            assertThatThrownBy(() -> reviewService.deleteReview(reviewId, hackerId, false))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION);

            verify(reviewRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Happy Path: Should delete when caller is the author")
        void shouldDeleteIfAuthor() {
            // Arrange
            UUID reviewId = UUID.randomUUID();

            Review existingReview = new Review();
            existingReview.setUserId(userId);

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // Act: owner (userId matches), not admin
            reviewService.deleteReview(reviewId, userId, false);

            // Assert
            verify(reviewRepository).delete(existingReview);
        }

        @Test
        @DisplayName("Happy Path: Should delete another user's review when caller is admin")
        void shouldDeleteIfAdmin() {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();

            Review existingReview = new Review();
            existingReview.setUserId(userId); // Belongs to someone else

            given(reviewRepository.findById(reviewId)).willReturn(Optional.of(existingReview));

            // Act: caller is NOT the owner but isAdmin=true
            reviewService.deleteReview(reviewId, adminId, true);

            // Assert
            verify(reviewRepository).delete(existingReview);
        }
    }
}