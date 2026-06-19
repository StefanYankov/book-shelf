package bg.softuni.bookshelf.data.repository;

import bg.softuni.bookshelf.config.JpaConfig;
import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.BookFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

    @Test
    @DisplayName("Should persist and retrieve a BookshelfBook entity with a composite key")
    void shouldPersistAndFindBookshelfBook() {
        // Arrange
        ApplicationUser user = new ApplicationUser();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        userRepository.save(user);

        Bookshelf shelf = new Bookshelf();
        shelf.setName("My Shelf");
        shelf.setUser(user);
        bookshelfRepository.save(shelf);

        Language lang = new Language();
        lang.setName("English");
        languageRepository.save(lang);

        Publisher pub = new Publisher();
        pub.setName("Allen & Unwin");
        publisherRepository.save(pub);

        Book book = Book.builder()
                .title("The Hobbit")
                .pages(320)
                .yearPublished(1937)
                .format(BookFormat.PAPERBACK)
                .summary("A fantasy novel.")
                .language(lang)
                .publisher(pub)
                .build();
        bookRepository.save(book);

        BookshelfBookId id = new BookshelfBookId();
        id.setBookshelfId(shelf.getId());
        id.setBookId(book.getId());

        BookshelfBook bookshelfBook = new BookshelfBook();
        bookshelfBook.setId(id);
        bookshelfBook.setBookshelf(shelf);
        bookshelfBook.setBook(book);

        bookshelfBookRepository.saveAndFlush(bookshelfBook);

        // Act
        Optional<BookshelfBook> found = bookshelfBookRepository.findById(id);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getBookshelf().getName()).isEqualTo("My Shelf");
        assertThat(found.get().getBook().getTitle()).isEqualTo("The Hobbit");
    }
}
