package bg.softuni.bookshelf.integration;

import bg.softuni.bookshelf.config.CacheConfig;
import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.data.repository.BookRepository;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.web.controller.WithMockApplicationUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Book Caching Integration Tests")
class BookCacheIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");
    private final UUID bookId = UUID.randomUUID();

    @Autowired
    private BookService bookService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private BookRepository bookRepository;

    private Book sampleBook() {
        Author author = new Author();
        author.setName("Test Author");
        Language language = new Language();
        language.setName("English");
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");

        Book book = Book.builder()
                .title("Cached Book")
                .ISBN("9780000000001")
                .pages(200)
                .yearPublished(2020)
                .summary("A summary.")
                .format(BookFormat.PAPERBACK)
                .author(author)
                .language(language)
                .publisher(publisher)
                .genres(Set.of())
                .build();
        book.setId(bookId);
        return book;
    }

    @AfterEach
    void clearCache() {
        java.util.Optional.ofNullable(cacheManager.getCache(CacheConfig.BOOKS_CACHE))
                .ifPresent(org.springframework.cache.Cache::clear);
    }

    @Test
    @DisplayName("A second getById is served from the cache without a repository call")
    void shouldServeSecondReadFromCache() {
        // Arrange
        given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(sampleBook()));

        // Act
        bookService.getById(bookId);
        bookService.getById(bookId);

        // Assert
        verify(bookRepository, times(1)).findBookDetailsById(bookId);
    }

    @Test
    @DisplayName("Updating a book evicts it from the cache, forcing a fresh read")
    void shouldEvictOnUpdate() {
        // Arrange
        given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.save(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

        // Act:
        bookService.getById(bookId);
        bookService.updateBook(bookId, BookUpdateDto.builder().title("New Title").build());
        bookService.getById(bookId);

        // Assert
        verify(bookRepository, times(2)).findBookDetailsById(bookId);
    }

    @Test
    @WithMockApplicationUser(roles = "ADMIN")
    @DisplayName("Moderating a book also evicts it from the cache")
    void shouldEvictOnModerate() {
        // Arrange
        given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.save(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

        // Act
        bookService.getById(bookId);
        bookService.moderateBook(bookId, BookUpdateDto.builder().title("Sanitized").build());
        bookService.getById(bookId);

        // Assert
        verify(bookRepository, times(2)).findBookDetailsById(bookId);
    }
}