package bg.softuni.bookshelf.service.review;

import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewMapper Unit Tests")
class ReviewMapperTest {

    private final ReviewMapper mapper = new ReviewMapper();

    @Test
    @DisplayName("toReviewEntity maps content and the ownership/target fields")
    void toEntity() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        ReviewCreateDto dto = new ReviewCreateDto("Great", "Loved it", 5);

        // Act
        Review review = mapper.toReviewEntity(dto, userId, targetId, "BOOK");

        // Assert
        assertThat(review.getUserId()).isEqualTo(userId);
        assertThat(review.getTargetId()).isEqualTo(targetId);
        assertThat(review.getTargetType()).isEqualTo("BOOK");
        assertThat(review.getTitle()).isEqualTo("Great");
        assertThat(review.getComment()).isEqualTo("Loved it");
        assertThat(review.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("toReviewViewDto maps all fields including the supplied username")
    void toView() {
        // Arrange
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Instant now = Instant.now();

        Review review = new Review();
        review.setId(id);
        review.setTitle("Great");
        review.setComment("Loved it");
        review.setRating(5);
        review.setUserId(userId);
        review.setTargetId(targetId);
        review.setTargetType("BOOK");
        ReflectionTestUtils.setField(review, "createdAt", now);
        ReflectionTestUtils.setField(review, "updatedAt", now);

        // Act
        ReviewViewDto dto = mapper.toReviewViewDto(review, "alice");

        // Assert
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Great");
        assertThat(dto.comment()).isEqualTo("Loved it");
        assertThat(dto.rating()).isEqualTo(5);
        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.username()).isEqualTo("alice");
        assertThat(dto.targetId()).isEqualTo(targetId);
        assertThat(dto.targetType()).isEqualTo("BOOK");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }
}