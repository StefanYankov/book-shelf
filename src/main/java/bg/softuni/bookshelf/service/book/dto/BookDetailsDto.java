package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for returning detailed information about a single book.
 */
public record BookDetailsDto(
        UUID id,
        String title,
        String isbn,
        int pages,
        int yearPublished,
        String summary,
        BookFormat format,
        AuthorDto author,
        LanguageDto language,
        PublisherDto publisher,
        Set<GenreDto> genres,
        String coverImageUrl
) {
}
