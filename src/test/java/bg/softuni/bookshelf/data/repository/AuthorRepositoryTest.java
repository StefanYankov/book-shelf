package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Author;
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
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Author Repository Integration Tests")
class AuthorRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("findByNameIgnoreCase(String) Tests")
    class FindByNameIgnoreCaseTests {

        @Test
        @DisplayName("Happy Path: Should find author with exact case")
        void shouldFindAuthorWithExactCase() {
            // Arrange
            Author author = new Author();
            author.setName("J.R.R. Tolkien");
            entityManager.persistAndFlush(author);

            // Act
            Optional<Author> found = authorRepository.findByNameIgnoreCase("J.R.R. Tolkien");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("J.R.R. Tolkien");
        }

        @Test
        @DisplayName("Happy Path: Should find author with different case")
        void shouldFindAuthorWithDifferentCase() {
            // Arrange
            Author author = new Author();
            author.setName("J.R.R. Tolkien");
            entityManager.persistAndFlush(author);

            // Act
            Optional<Author> found = authorRepository.findByNameIgnoreCase("j.r.r. tolkien");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("J.R.R. Tolkien");
        }
    }
}
