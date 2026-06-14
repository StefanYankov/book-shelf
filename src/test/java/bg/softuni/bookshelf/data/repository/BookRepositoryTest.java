package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.enums.BookFormat;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    // --- TEST DATA FACTORY ---

    private Author createAndSaveAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return entityManager.persistAndFlush(author);
    }

    private Language createAndSaveLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return entityManager.persistAndFlush(language);
    }

    private Publisher createAndSavePublisher(String name) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        return entityManager.persistAndFlush(publisher);
    }

    private void createAndSaveBook(String title, Author author, Language lang, Publisher pub) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .language(lang)
                .publisher(pub)
                .format(BookFormat.PAPERBACK)
                .ISBN("978-0345339683")
                .pages(320)
                .yearPublished(1937)
                .summary("A fantasy novel.")
                .build();
        bookRepository.saveAndFlush(book);
    }

    @Nested
    @DisplayName("findAllWithAuthors() Tests")
    class FindAllWithAuthorsTests {

        @Test
        @DisplayName("Happy Path: Should return books with authors eagerly fetched")
        void shouldReturnBooksWithAuthorsEagerlyFetched() {
            // Arrange
            Author author = createAndSaveAuthor("J.R.R. Tolkien");
            Language lang = createAndSaveLanguage("English");
            Publisher pub = createAndSavePublisher("Allen & Unwin");
            createAndSaveBook("The Hobbit", author, lang, pub);
            entityManager.clear();

            // Act
            List<Book> books = bookRepository.findAllWithAuthors();

            // Assert
            assertThat(books).hasSize(1);
            Book fetchedBook = books.getFirst();

            assertThat(fetchedBook.getAuthor().getName()).isEqualTo("J.R.R. Tolkien");
        }

        @Test
        @DisplayName("Edge Case: Should return empty list when no books exist")
        void shouldReturnEmptyListWhenNoBooks() {
            // Act
            List<Book> books = bookRepository.findAllWithAuthors();

            // Assert
            assertThat(books).isEmpty();
        }
    }
}
