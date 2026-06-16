package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Publisher;
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
@DisplayName("Publisher Repository Integration Tests")
class PublisherRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("findByNameIgnoreCase(String) Tests")
    class FindByNameIgnoreCaseTests {

        @Test
        @DisplayName("Happy Path: Should find publisher with exact case")
        void shouldFindPublisherWithExactCase() {
            // Arrange
            Publisher publisher = new Publisher();
            publisher.setName("Prosveta");
            entityManager.persistAndFlush(publisher);

            // Act
            Optional<Publisher> found = publisherRepository.findByNameIgnoreCase("Prosveta");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Prosveta");
        }

        @Test
        @DisplayName("Happy Path: Should find publisher with different case")
        void shouldFindPublisherWithDifferentCase() {
            // Arrange
            Publisher publisher = new Publisher();
            publisher.setName("Prosveta");
            entityManager.persistAndFlush(publisher);

            // Act
            Optional<Publisher> found = publisherRepository.findByNameIgnoreCase("prosveta");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Prosveta");
        }

        @Test
        @DisplayName("Edge Case: Should return empty optional for non-existent publisher")
        void shouldReturnEmptyForNonExistentPublisher() {
            // Act
            Optional<Publisher> found = publisherRepository.findByNameIgnoreCase("NonExistent");

            // Assert
            assertThat(found).isEmpty();
        }
    }
}
