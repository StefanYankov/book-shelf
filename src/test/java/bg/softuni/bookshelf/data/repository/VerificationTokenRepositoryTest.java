package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.TokenType;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VerificationTokenRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    // --- TEST DATA FACTORY ---

    private User createAndSaveUser(String username) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("hashed-password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        user.setEmailVerified(false);
        return userRepository.saveAndFlush(user);
    }

    private void createAndSaveToken(User user, String hash, TokenType type, Instant expiry) {
        VerificationToken token = VerificationToken.builder()
                .tokenHash(hash)
                .tokenType(type)
                .expiryDate(expiry)
                .user(user)
                .build();
        verificationTokenRepository.saveAndFlush(token);
    }

    @Nested
    @DisplayName("findByTokenHash(String) Tests")
    class FindByTokenHashTests {

        @Test
        @DisplayName("Happy Path: Should find token when it exists")
        void shouldFindTokenWhenExists() {
            // Arrange
            User user = createAndSaveUser("tokenuser1");
            String hash = "a".repeat(64); // Simulate SHA-256 hash
            createAndSaveToken(user, hash, TokenType.EMAIL_VERIFICATION, Instant.now().plus(1, ChronoUnit.HOURS));

            // Act
            Optional<VerificationToken> result = verificationTokenRepository.findByTokenHash(hash);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getTokenHash()).isEqualTo(hash);
            assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("Error Case: Should return empty when token does not exist")
        void shouldReturnEmptyWhenTokenDoesNotExist() {
            // Act
            Optional<VerificationToken> result = verificationTokenRepository.findByTokenHash("nonexistent");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(UUID, TokenType) Tests")
    class FindFirstByUserAndTypeTests {

        @Test
        @DisplayName("Happy Path: Should return the most recently created token for a user and type")
        void shouldReturnMostRecentToken() throws InterruptedException {
            // Arrange
            User user = createAndSaveUser("multi-token-user");
            String oldHash = "a".repeat(64);
            String newHash = "b".repeat(64);

            createAndSaveToken(user, oldHash, TokenType.PASSWORD_RESET, Instant.now().plus(1, ChronoUnit.HOURS));
            Thread.sleep(10); // Ensure distinct 'createdAt' timestamps
            createAndSaveToken(user, newHash, TokenType.PASSWORD_RESET, Instant.now().plus(1, ChronoUnit.HOURS));

            // Act
            Optional<VerificationToken> result = verificationTokenRepository.findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(user.getId(), TokenType.PASSWORD_RESET);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getTokenHash()).isEqualTo(newHash);
        }

        @Test
        @DisplayName("Edge Case: Should only return tokens matching the specific TokenType")
        void shouldOnlyReturnMatchingType() {
            // Arrange
            User user = createAndSaveUser("mixed-token-user");
            String emailHash = "c".repeat(64);
            String resetHash = "d".repeat(64);

            createAndSaveToken(user, emailHash, TokenType.EMAIL_VERIFICATION, Instant.now().plus(1, ChronoUnit.HOURS));
            createAndSaveToken(user, resetHash, TokenType.PASSWORD_RESET, Instant.now().plus(1, ChronoUnit.HOURS));

            // Act
            Optional<VerificationToken> result = verificationTokenRepository.findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(user.getId(), TokenType.PASSWORD_RESET);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getTokenHash()).isEqualTo(resetHash);
            assertThat(result.get().getTokenType()).isEqualTo(TokenType.PASSWORD_RESET);
        }
    }
}
