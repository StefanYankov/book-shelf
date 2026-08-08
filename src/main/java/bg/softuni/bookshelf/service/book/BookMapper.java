package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.service.book.dto.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component responsible for mapping between Book entities and their corresponding DTOs.
 * This separation of concerns keeps the service layer clean from mapping logic.
 */
@Component
public class BookMapper {

    /**
     * Maps a {@link BookCreateDto} and its related entities to a new {@link Book} entity.
     *
     * @param dto       The source DTO containing the book's creation data.
     * @param author    The {@link Author} entity corresponding to the authorId in the DTO.
     * @param language  The {@link Language} entity corresponding to the languageId in the DTO.
     * @param publisher The {@link Publisher} entity corresponding to the publisherId in the DTO.
     * @param genres    The set of {@link Genre} entities corresponding to the genreIds in the DTO.
     * @return A new, transient {@link Book} entity ready for persistence.
     */
    public Book toBookEntity(BookCreateDto dto, Author author, Language language, Publisher publisher, Set<Genre> genres) {
        return Book.builder()
                .title(dto.title())
                .ISBN(dto.isbn())
                .pages(dto.pages())
                .yearPublished(dto.yearPublished())
                .summary(dto.summary())
                .format(dto.format())
                .author(author)
                .language(language)
                .publisher(publisher)
                .genres(genres)
                .build();
    }

    /**
     * Maps a {@link Book} entity to a {@link BookDetailsDto} for detailed client-side display.
     *
     * @param book The persistent {@link Book} entity.
     * @return A comprehensive {@link BookDetailsDto}.
     */
    public BookDetailsDto toBookDetailsDto(Book book) {
        return new BookDetailsDto(
                book.getId(),
                book.getTitle(),
                book.getISBN(),
                book.getPages(),
                book.getYearPublished(),
                book.getSummary(),
                book.getFormat(),
                toAuthorDto(book.getAuthor()),
                toLanguageDto(book.getLanguage()),
                toPublisherDto(book.getPublisher()),
                book.getGenres().stream().map(this::toGenreDto).collect(Collectors.toSet()),
                book.getCoverImage() != null ? book.getCoverImage().getUrl() : null
        );
    }

    /**
     * Maps a {@link Book} entity to a {@link BookSummaryDto} for list displays.
     *
     * @param book The persistent {@link Book} entity.
     * @return A lightweight {@link BookSummaryDto}.
     */
    public BookSummaryDto toBookSummaryDto(Book book) {
        return new BookSummaryDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getName(),
                book.getCoverImage() != null ? book.getCoverImage().getUrl() : null
        );
    }

    private BookAuthorDto toAuthorDto(Author author) {
        return new BookAuthorDto(author.getId(), author.getName());
    }

    private BookLanguageDto toLanguageDto(Language language) {
        return new BookLanguageDto(language.getId(), language.getName());
    }

    private BookPublisherDto toPublisherDto(Publisher publisher) {
        return new BookPublisherDto(publisher.getId(), publisher.getName());
    }

    private BookGenreDto toGenreDto(Genre genre) {
        return new BookGenreDto(genre.getId(), genre.getName());
    }
}
