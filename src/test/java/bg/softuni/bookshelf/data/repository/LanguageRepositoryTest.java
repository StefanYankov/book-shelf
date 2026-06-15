package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Language Repository Integration Tests")
class LanguageRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("findByNameIgnoreCase(String) Tests")
    class FindByNameIgnoreCaseTests {

        @Test
        @DisplayName("Happy Path: Should find language with exact case")
        void shouldFindLanguageWithExactCase() {
            // Arrange
            Language language = new Language();
            language.setName("English");
            entityManager.persistAndFlush(language);

            // Act
            Optional<Language> found = languageRepository.findByNameIgnoreCase("English");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("English");
        }

        @Test
        @DisplayName("Happy Path: Should find language with different case")
        void shouldFindLanguageWithDifferentCase() {
            // Arrange
            Language language = new Language();
            language.setName("English");
            entityManager.persistAndFlush(language);

            // Act
            Optional<Language> found = languageRepository.findByNameIgnoreCase("english");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("English");
        }

        @Test
        @DisplayName("Edge Case: Should return empty optional for non-existent language")
        void shouldReturnEmptyForNonExistentLanguage() {
            // Act
            Optional<Language> found = languageRepository.findByNameIgnoreCase("NonExistent");

            // Assert
            assertThat(found).isEmpty();
        }
    }
}
