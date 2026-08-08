package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for returning detailed information about a single book.
 */
@Builder
public record BookDetailsDto(
        UUID id,
        String title,
        String isbn,
        int pages,
        int yearPublished,
        String summary,
        BookFormat format,
        BookAuthorDto author,
        BookLanguageDto language,
        BookPublisherDto publisher,
        Set<BookGenreDto> genres,
        String coverImageUrl
) {
}
