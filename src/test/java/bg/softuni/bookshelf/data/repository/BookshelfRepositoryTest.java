package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookshelfRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private UserRepository userRepository;

    private ApplicationUser targetUser;
    private ApplicationUser otherUser;

    @BeforeEach
    void setUp() {
        bookshelfRepository.deleteAll();
        userRepository.deleteAll();

        targetUser = new ApplicationUser();
        targetUser.setUsername("targetUser");
        targetUser.setEmail("target@example.com");
        targetUser.setPassword("password");
        targetUser.setFirstName("Target");
        targetUser.setLastName("User");
        userRepository.save(targetUser);

        otherUser = new ApplicationUser();
        otherUser.setUsername("otherUser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        userRepository.save(otherUser);
    }

    @Nested
    @DisplayName("findAllByUser_Id Tests")
    class FindAllByUserIdTests {

        @Test
        @DisplayName("Happy Path: Should find all bookshelves for a specific user")
        void shouldFindAllBookshelvesForUser() {
            // Arrange
            Bookshelf shelf1 = new Bookshelf();
            shelf1.setName("Shelf 1");
            shelf1.setUser(targetUser);
            bookshelfRepository.save(shelf1);

            Bookshelf shelf2 = new Bookshelf();
            shelf2.setName("Shelf 2");
            shelf2.setUser(targetUser);
            bookshelfRepository.save(shelf2);

            Bookshelf otherShelf = new Bookshelf();
            otherShelf.setName("Other User Shelf");
            otherShelf.setUser(otherUser);
            bookshelfRepository.save(otherShelf);

            // Act
            Page<Bookshelf> result = bookshelfRepository.findAllByUser_Id(targetUser.getId(), PageRequest.of(0, 10));

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).extracting(Bookshelf::getName).containsExactlyInAnyOrder("Shelf 1", "Shelf 2");
        }

        @Test
        @DisplayName("Empty Case: Should return empty page for user with no shelves")
        void shouldReturnEmptyPageForUserWithNoShelves() {
            // Act
            Page<Bookshelf> result = bookshelfRepository.findAllByUser_Id(targetUser.getId(), PageRequest.of(0, 10));

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Not Found Case: Should return empty page for non-existent user ID")
        void shouldReturnEmptyPageForNonExistentUser() {
            // Act
            Page<Bookshelf> result = bookshelfRepository.findAllByUser_Id(UUID.randomUUID(), PageRequest.of(0, 10));

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Pagination Case: Should return correct page slice and sorting")
        void shouldReturnCorrectPageSliceAndSort() {
            // Arrange
            IntStream.range(1, 16).forEach(i -> {
                Bookshelf shelf = new Bookshelf();
                shelf.setName(String.format("Shelf %02d", i));
                shelf.setUser(targetUser);
                bookshelfRepository.save(shelf);
            });

            Pageable pageable = PageRequest.of(1, 5, Sort.by("name").descending());

            // Act
            Page<Bookshelf> result = bookshelfRepository.findAllByUser_Id(targetUser.getId(), pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getContent().getFirst().getName()).isEqualTo("Shelf 10");
            assertThat(result.getContent().getLast().getName()).isEqualTo("Shelf 06");
        }
    }
}
