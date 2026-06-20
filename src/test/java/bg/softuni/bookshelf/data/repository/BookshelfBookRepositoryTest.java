package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.BookFormat;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookshelfBookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private BookshelfBookRepository bookshelfBookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookshelfRepository bookshelfRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private ApplicationUser testUser;
    private Language testLang;
    private Publisher testPub;

    @BeforeEach
    void setUp() {
        bookshelfBookRepository.deleteAll();
        bookRepository.deleteAll();
        bookshelfRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new ApplicationUser();
        testUser.setUsername("user");
        testUser.setEmail("user@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("First");
        testUser.setLastName("Last");
        userRepository.save(testUser);

        testLang = new Language();
        testLang.setName("English");
        languageRepository.save(testLang);

        testPub = new Publisher();
        testPub.setName("Allen & Unwin");
        publisherRepository.save(testPub);
    }

    // --- TEST DATA FACTORIES ---

    private Bookshelf createAndSaveShelf(String name, ApplicationUser user) {
        Bookshelf shelf = new Bookshelf();
        shelf.setName(name);
        shelf.setUser(user);
        return bookshelfRepository.save(shelf);
    }

    private Book createAndSaveBook(String title, Language lang, Publisher pub) {
        Book book = Book.builder()
                .title(title)
                .pages(100)
                .yearPublished(2000)
                .format(BookFormat.PAPERBACK)
                .summary("Summary")
                .language(lang)
                .publisher(pub)
                .build();
        return bookRepository.save(book);
    }

    private BookshelfBookId createBookshelfBook(Bookshelf shelf, Book book) {
        BookshelfBookId id = new BookshelfBookId();
        id.setBookshelfId(shelf.getId());
        id.setBookId(book.getId());

        BookshelfBook bookshelfBook = new BookshelfBook();
        bookshelfBook.setId(id);
        bookshelfBook.setBookshelf(shelf);
        bookshelfBook.setBook(book);
        bookshelfBookRepository.saveAndFlush(bookshelfBook);
        return id;
    }

    @Test
    @DisplayName("Should persist and retrieve a BookshelfBook entity with a composite key")
    void shouldPersistAndFindBookshelfBook() {
        // Arrange
        Bookshelf shelf = createAndSaveShelf("My Shelf", testUser);
        Book book = createAndSaveBook("The Hobbit", testLang, testPub);
        BookshelfBookId id = createBookshelfBook(shelf, book);

        // Act
        Optional<BookshelfBook> found = bookshelfBookRepository.findById(id);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
    }

    @Nested
    @DisplayName("findBooksByBookshelfId Tests")
    class FindBooksByBookshelfIdTests {

        @Test
        @DisplayName("Should return paginated books for a specific shelf")
        void shouldReturnPaginatedBooksForShelf() {
            // Arrange
            Bookshelf shelf1 = createAndSaveShelf("Shelf 1", testUser);
            Bookshelf shelf2 = createAndSaveShelf("Shelf 2", testUser);

            Book book1 = createAndSaveBook("Book A", testLang, testPub);
            Book book2 = createAndSaveBook("Book B", testLang, testPub);
            Book book3 = createAndSaveBook("Book C", testLang, testPub);

            createBookshelfBook(shelf1, book1);
            createBookshelfBook(shelf1, book2);
            createBookshelfBook(shelf2, book3);

            Pageable pageable = PageRequest.of(0, 5, Sort.by("book.title").ascending());

            // Act
            Page<Book> result = bookshelfBookRepository.findBooksByBookshelfId(shelf1.getId(), pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).extracting(Book::getTitle).containsExactly("Book A", "Book B");
        }

        @Test
        @DisplayName("Should return empty page for a shelf with no books")
        void shouldReturnEmptyPageForEmptyShelf() {
            // Arrange
            Bookshelf emptyShelf = createAndSaveShelf("Empty Shelf", testUser);
            Pageable pageable = PageRequest.of(0, 5);

            // Act
            Page<Book> result = bookshelfBookRepository.findBooksByBookshelfId(emptyShelf.getId(), pageable);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
