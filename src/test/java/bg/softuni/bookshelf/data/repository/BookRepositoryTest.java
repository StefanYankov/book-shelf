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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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

    private void createAndSaveBook(String title, String isbn, Author author, Language lang, Publisher pub) {
        Book book = Book.builder()
                .title(title)
                .ISBN(isbn)
                .author(author)
                .language(lang)
                .publisher(pub)
                .format(BookFormat.PAPERBACK)
                .pages(320)
                .yearPublished(1937)
                .summary("A fantasy novel.")
                .build();
        bookRepository.saveAndFlush(book);
    }

    @Nested
    @DisplayName("findAllWithAuthors(Pageable) Tests")
    class FindAllWithAuthorsTests {

        @Test
        @DisplayName("Should return paginated books with authors eagerly fetched")
        void shouldReturnPaginatedBooksWithAuthors() {
            // Arrange
            Author author1 = createAndSaveAuthor("J.R.R. Tolkien");
            Author author2 = createAndSaveAuthor("Frank Herbert");
            Language lang = createAndSaveLanguage("English");
            Publisher pub = createAndSavePublisher("Allen & Unwin");

            createAndSaveBook("The Hobbit", "978-0-345-33968-3", author1, lang, pub);
            createAndSaveBook("Dune", "978-0-441-01359-3", author2, lang, pub);
            createAndSaveBook("The Lord of the Rings", "978-0-618-64015-7", author1, lang, pub);
            entityManager.clear();

            // Act
            Page<Book> resultPage = bookRepository.findAllWithAuthors(PageRequest.of(0, 2));

            // Assert
            assertThat(resultPage.getTotalElements()).isEqualTo(3);
            assertThat(resultPage.getContent()).hasSize(2);
            assertThat(resultPage.getContent().getFirst().getAuthor().getName()).isNotNull();
        }

        @Test
        @DisplayName("Should return empty page when no books exist")
        void shouldReturnEmptyPageWhenNoBooks() {
            // Act
            Page<Book> resultPage = bookRepository.findAllWithAuthors(PageRequest.of(0, 20));

            // Assert
            assertThat(resultPage).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByAuthorId(UUID, Pageable) Tests")
    class FindAllByAuthorIdTests {

        @Test
        @DisplayName("Should return paginated books for the specified author")
        void shouldReturnPaginatedBooksForAuthor() {
            // Arrange
            Author author1 = createAndSaveAuthor("J.R.R. Tolkien");
            Author author2 = createAndSaveAuthor("Frank Herbert");
            Language lang = createAndSaveLanguage("English");
            Publisher pub = createAndSavePublisher("Allen & Unwin");

            createAndSaveBook("The Hobbit", "978-0-345-33968-3", author1, lang, pub);
            createAndSaveBook("Dune", "978-0-441-01359-3", author2, lang, pub);
            createAndSaveBook("The Lord of the Rings", "978-0-618-64015-7", author1, lang, pub);
            entityManager.clear();

            // Act
            Page<Book> tolkienBooksPage = bookRepository.findAllByAuthorId(author1.getId(), PageRequest.of(0, 1));

            // Assert
            assertThat(tolkienBooksPage.getTotalElements()).isEqualTo(2);
            assertThat(tolkienBooksPage.getContent()).hasSize(1);
        }
    }
}
