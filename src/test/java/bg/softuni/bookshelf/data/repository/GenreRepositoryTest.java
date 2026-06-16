package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Genre;
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
@DisplayName("Genre Repository Integration Tests")
class GenreRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("findByNameIgnoreCase(String) Tests")
    class FindByNameIgnoreCaseTests {

        @Test
        @DisplayName("Happy Path: Should find genre with exact case")
        void shouldFindGenreWithExactCase() {
            // Arrange
            Genre genre = new Genre();
            genre.setName("Fantasy");
            entityManager.persistAndFlush(genre);

            // Act
            Optional<Genre> found = genreRepository.findByNameIgnoreCase("Fantasy");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Fantasy");
        }

        @Test
        @DisplayName("Happy Path: Should find genre with different case")
        void shouldFindGenreWithDifferentCase() {
            // Arrange
            Genre genre = new Genre();
            genre.setName("Fantasy");
            entityManager.persistAndFlush(genre);

            // Act
            Optional<Genre> found = genreRepository.findByNameIgnoreCase("fantasy");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Fantasy");
        }

        @Test
        @DisplayName("Edge Case: Should return empty optional for non-existent genre")
        void shouldReturnEmptyForNonExistentGenre() {
            // Act
            Optional<Genre> found = genreRepository.findByNameIgnoreCase("NonExistent");

            // Assert
            assertThat(found).isEmpty();
        }
    }
}
