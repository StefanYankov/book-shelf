package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private UserRepository userRepository;

    // --- TEST DATA FACTORY ---

    private ApplicationUser createApplicationUserEntity(String username, String email) {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(username);
        applicationUser.setEmail(email);
        applicationUser.setPassword("hashed-password");
        applicationUser.setFirstName("Test");
        applicationUser.setLastName("User");

        return applicationUser;
    }

    @Nested
    @DisplayName("findById(UUID) Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find user when ID exists")
        void shouldFindUserWhenIdExists() {
            // Arrange
            ApplicationUser savedUser = userRepository.saveAndFlush(createApplicationUserEntity("testuser", "test@example.com"));

            // Act
            Optional<User> result = userRepository.findById(savedUser.getId());

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("Should return empty when ID does not exist")
        void shouldReturnEmptyWhenIdDoesNotExist() {
            // Act
            Optional<User> result = userRepository.findById(UUID.randomUUID());

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsername(String) Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Happy Path: Should find user when exact username exists")
        void shouldFindUserWhenUsernameExists() {

            // Arrange
            userRepository.saveAndFlush(createApplicationUserEntity("testuser", "test@example.com"));

            // Act
            Optional<User> result = userRepository.findByUsername("testuser");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Error Case: Should return empty when username does not exist")
        void shouldReturnEmptyWhenUsernameNotFound() {

            // Act
            Optional<User> result = userRepository.findByUsername("nonexistent");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Edge Case: Should return empty when username case differs")
        void shouldReturnEmptyWhenCaseIsDifferent() {

            // Arrange
            userRepository.saveAndFlush(createApplicationUserEntity("testuser", "test@example.com"));

            // Act
            Optional<User> result = userRepository.findByUsername("TestUser");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail(String) Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Happy Path: Should find user when exact email exists")
        void shouldFindUserWhenEmailExists() {

            // Arrange
            userRepository.saveAndFlush(createApplicationUserEntity("testuser", "test@example.com"));

            // Act
            Optional<User> result = userRepository.findByEmail("test@example.com");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Error Case: Should return empty when email does not exist")
        void shouldReturnEmptyWhenEmailNotFound() {

            // Act
            Optional<User> result = userRepository.findByEmail("wrong@example.com");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Database Constraint Tests")
    class ConstraintTests {

        @Test
        @DisplayName("Error Case: Should throw DataIntegrityViolationException on duplicate username")
        void shouldThrowOnDuplicateUsername() {

            // Arrange
            userRepository.saveAndFlush(createApplicationUserEntity("testuser", "one@example.com"));
            ApplicationUser duplicateUser = createApplicationUserEntity("testuser", "two@example.com");

            // Act & Assert
            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uc_users_username");
        }

        @Test
        @DisplayName("Error Case: Should throw DataIntegrityViolationException on duplicate email")
        void shouldThrowOnDuplicateEmail() {

            // Arrange
            userRepository.saveAndFlush(createApplicationUserEntity("userone", "test@example.com"));
            ApplicationUser duplicateEmail = createApplicationUserEntity("usertwo", "test@example.com");

            // Act & Assert
            assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateEmail))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uc_users_email");
        }
    }
}
