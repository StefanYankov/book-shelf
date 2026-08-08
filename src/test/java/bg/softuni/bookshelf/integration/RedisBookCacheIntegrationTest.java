package bg.softuni.bookshelf.integration;

import bg.softuni.bookshelf.config.CacheConfig;
import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.data.repository.BookRepository;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verifies the Redis-backed cache path against a Redis Testcontainer: a value round-trips through
 * the JSON serializer and eviction removes it. The repository is mocked, so no seed data is needed.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Redis Book Caching Integration Tests")
class RedisBookCacheIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
    private final UUID bookId = UUID.randomUUID();

    @Autowired
    private BookService bookService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private BookRepository bookRepository;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cache.type", () -> "redis");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

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

    private Cache booksCache() {
        return Objects.requireNonNull(cacheManager.getCache(CacheConfig.BOOKS_CACHE));
    }

    @AfterEach
    void clearCache() {
        Optional.ofNullable(cacheManager.getCache(CacheConfig.BOOKS_CACHE)).ifPresent(Cache::clear);
    }

    @Test
    @DisplayName("The active cache manager is Redis-backed")
    void cacheManagerIsRedis() {
        // Assert
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
    }

    @Test
    @DisplayName("A value round-trips through the Redis JSON serializer as its type")
    void valueRoundTripsThroughRedis() {
        // Arrange
        given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(sampleBook()));

        // Act
        BookDetailsDto first = bookService.getById(bookId);
        BookDetailsDto cached = booksCache().get(bookId, BookDetailsDto.class);

        // Assert
        assertThat(cached).isNotNull();
        assertThat(cached.id()).isEqualTo(first.id());
        assertThat(cached.title()).isEqualTo("Cached Book");
        assertThat(cached.pages()).isEqualTo(200);
        assertThat(cached.format()).isEqualTo(BookFormat.PAPERBACK);
    }

    @Test
    @DisplayName("A second read is served from Redis without a repository call")
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
    @DisplayName("Updating a book evicts it from Redis, forcing a fresh read")
    void shouldEvictOnUpdate() {
        // Arrange
        given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(sampleBook()));
        given(bookRepository.save(any(Book.class))).willAnswer(inv -> inv.getArgument(0));

        // Act
        bookService.getById(bookId);
        assertThat(booksCache().get(bookId)).isNotNull();
        bookService.updateBook(bookId, BookUpdateDto.builder().title("New Title").build());

        // Assert
        assertThat(booksCache().get(bookId)).isNull();
        bookService.getById(bookId);
        verify(bookRepository, times(2)).findBookDetailsById(bookId);
    }
}