package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for returning detailed information about a single book.
 */
@Builder
public record BookDetailsDto(
        @Schema(description = "Unique book identifier.",
                example = "66666666-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Book title.", example = "Dune")
        String title,

        @Schema(description = "International Standard Book Number.", example = "978-0441013593")
        String isbn,

        @Schema(description = "Total number of pages.", example = "412")
        int pages,

        @Schema(description = "Year of publication.", example = "1965")
        int yearPublished,

        @Schema(description = "Short synopsis of the book.",
                example = "A desert planet, a noble house, and the struggle for a rare resource.")
        String summary,

        @Schema(description = "Physical or digital format of the book.", example = "HARDCOVER")
        BookFormat format,

        BookAuthorDto author,

        BookLanguageDto language,

        BookPublisherDto publisher,

        @Schema(description = "Genres associated with the book.")
        Set<BookGenreDto> genres,

        @Schema(description = "URL of the cover image; empty when none is set.",
                example = "https://res.cloudinary.com/demo/image/upload/dune.jpg")
        String coverImageUrl
) {
}
