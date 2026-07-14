package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Review;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ReviewRepository Data Slice Tests")
class ReviewRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- TEST DATA FACTORY ---

    private ApplicationUser createAndSaveUser(String username) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("hashed_password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmailVerified(true);
        user.setPasswordChangeRequired(false);
        return entityManager.persistAndFlush(user);
    }

    private Review createAndSaveReview(ApplicationUser user, UUID targetId, String targetType) {
        Review review = new Review();
        review.setUserId(user.getId());
        review.setTargetId(targetId);
        review.setTargetType(targetType);
        review.setTitle("Test Title");
        review.setComment("Test Comment");
        review.setRating(5);
        return entityManager.persistAndFlush(review);
    }

    @Nested
    @DisplayName("Unique Constraint Tests")
    class UniqueConstraintTests {

        @Test
        @DisplayName("Should throw DataIntegrityViolationException when user reviews same target twice")
        void shouldThrowOnDuplicateReview() {
            // Arrange
            ApplicationUser user = createAndSaveUser("duplicateUser");
            UUID targetId = UUID.randomUUID();
            String targetType = "BOOK";

            createAndSaveReview(user, targetId, targetType);

            // Act & Assert
            Review duplicateReview = new Review();
            duplicateReview.setUserId(user.getId());
            duplicateReview.setTargetId(targetId);
            duplicateReview.setTargetType(targetType);
            duplicateReview.setTitle("Another Review");
            duplicateReview.setComment("Another Comment");
            duplicateReview.setRating(3);

            assertThatThrownBy(() -> reviewRepository.saveAndFlush(duplicateReview))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("Custom Query Tests")
    class CustomQueryTests {

        @Test
        @DisplayName("findAllByTargetIdAndTargetType: Should return paginated results")
        void shouldReturnPaginatedResults() {
            // Arrange
            ApplicationUser user1 = createAndSaveUser("user1");
            ApplicationUser user2 = createAndSaveUser("user2");
            ApplicationUser user3 = createAndSaveUser("user3");

            UUID targetId = UUID.randomUUID();
            String targetType = "BOOK";

            createAndSaveReview(user1, targetId, targetType);
            createAndSaveReview(user2, targetId, targetType);
            createAndSaveReview(user3, UUID.randomUUID(), targetType); // Different target
            entityManager.clear();

            // Act
            Page<Review> results = reviewRepository.findAllByTargetIdAndTargetType(targetId, targetType, PageRequest.of(0, 10));

            // Assert
            assertThat(results.getTotalElements()).isEqualTo(2);
            assertThat(results.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("existsByUserIdAndTargetIdAndTargetType: Should return true when exists")
        void shouldReturnTrueWhenExists() {
            // Arrange
            ApplicationUser user = createAndSaveUser("existsUser");
            UUID targetId = UUID.randomUUID();
            String targetType = "AUTHOR";

            createAndSaveReview(user, targetId, targetType);
            entityManager.clear();

            // Act
            boolean exists = reviewRepository.existsByUserIdAndTargetIdAndTargetType(user.getId(), targetId, targetType);

            // Assert
            assertThat(exists).isTrue();
        }
    }
}