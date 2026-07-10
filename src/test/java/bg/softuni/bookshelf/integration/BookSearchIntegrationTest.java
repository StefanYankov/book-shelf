package bg.softuni.bookshelf.integration;

import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.data.repository.*;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookSearchFilters;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("BookService Advanced Catalog Search Integration Tests")
class BookSearchIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    private Author tolkien;
    private Author sanderson;
    private Genre fantasy;
    private Genre sciFi;
    private Language english;
    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
        languageRepository.deleteAll();
        publisherRepository.deleteAll();

        // Seed relational dependencies
        tolkien = createAndSaveAuthor("J.R.R. Tolkien");
        sanderson = createAndSaveAuthor("Brandon Sanderson");
        fantasy = createAndSaveGenre("Fantasy");
        sciFi = createAndSaveGenre("Sci-Fi");
        english = createAndSaveLanguage("English");
        testPublisher = createAndSavePublisher("Test Publisher");

        createAndSaveBook("The Fellowship of the Ring", "9780261103573", BookFormat.PAPERBACK, 1954, tolkien, Set.of(fantasy), english, testPublisher);
        createAndSaveBook("Mistborn: The Final Empire", "9780765311788", BookFormat.HARDCOVER, 2006, sanderson, Set.of(fantasy), english, testPublisher);
        createAndSaveBook("Skyward", "9780525643111", BookFormat.PAPERBACK, 2018, sanderson, Set.of(sciFi), english, testPublisher);
    }

    // --- TEST DATA FACTORY METHODS ---

    private Author createAndSaveAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return authorRepository.save(author);
    }

    private Genre createAndSaveGenre(String name) {
        Genre genre = new Genre();
        genre.setName(name);
        return genreRepository.save(genre);
    }

    private Language createAndSaveLanguage(String name) {
        Language language = new Language();
        language.setName(name);
        return languageRepository.save(language);
    }

    private Publisher createAndSavePublisher(String name) {
        Publisher publisher = new Publisher();
        publisher.setName(name);
        return publisherRepository.save(publisher);
    }

    private void createAndSaveBook(
            String title,
            String isbn,
            BookFormat format,
            int yearPublished,
            Author author,
            Set<Genre> genres,
            Language language,
            Publisher publisher
    ) {
        Book book = new Book();
        book.setTitle(title);
        book.setISBN(isbn);
        book.setFormat(format);
        book.setYearPublished(yearPublished);
        book.setAuthor(author);
        book.setGenres(genres);
        book.setPages(300);
        book.setSummary("Faceted search test book placeholder summary.");
        book.setLanguage(language);
        book.setPublisher(publisher);
        bookRepository.save(book);
    }

    // --- TEST CASES REFACFTORING ---

    @Test
    @DisplayName("Should find books matching partial title search case-insensitively")
    void shouldFindBooksByTitleMatchesCaseInsensitively() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchFilters filters = new BookSearchFilters("fellowship", Collections.emptySet(), null, null, null);

        PagedResponse<BookSummaryDto> result = bookService.searchBooks(filters, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().title()).isEqualTo("The Fellowship of the Ring");
    }

    @Test
    @DisplayName("Should retrieve records matching exact faceted genre classifications")
    void shouldFindBooksFilteredByFacetedGenreNames() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchFilters filters = new BookSearchFilters(null, Set.of("Sci-Fi"), null, null, null);

        PagedResponse<BookSummaryDto> result = bookService.searchBooks(filters, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().title()).isEqualTo("Skyward");
    }

    @Test
    @DisplayName("Should extract records bound within lower and upper publication years")
    void shouldFindBooksBoundedByPublicationYearRangeAndFormat() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchFilters filters = new BookSearchFilters(null, Collections.emptySet(), BookFormat.PAPERBACK, 2000, 2020);

        PagedResponse<BookSummaryDto> result = bookService.searchBooks(filters, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().title()).isEqualTo("Skyward");
    }

    @Test
    @DisplayName("Should yield an empty result page when criteria match zero database records")
    void shouldReturnEmptyPageWhenNoRecordsMatchSearchFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchFilters filters = new BookSearchFilters("Non-existent book", Collections.emptySet(), null, null, null);

        PagedResponse<BookSummaryDto> result = bookService.searchBooks(filters, pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}