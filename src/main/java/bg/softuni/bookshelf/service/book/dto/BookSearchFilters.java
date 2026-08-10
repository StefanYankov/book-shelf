package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * A data transfer object that encapsulates all possible filter criteria for a book search.
 * Used as a @ParameterObject in the BookController for a cleaner API signature.
 *
 * @param query     A string to match against book titles or author names.
 * @param genres    A set of genre names to filter by.
 * @param format    The specific book format to filter by.
 * @param yearMin   The minimum publication year (inclusive).
 * @param yearMax   The maximum publication year (inclusive).
 */
public record BookSearchFilters(
        @Schema(description = "Free-text match against book titles or author names.",
                example = "dune")
        String query,

        @Schema(description = "Genre names to filter by.",
                example = "[\"Science Fiction\", \"Adventure\"]")
        Set<String> genres,

        @Schema(description = "Book format to filter by.", example = "HARDCOVER")
        BookFormat format,

        @Schema(description = "Minimum publication year, inclusive.", example = "1950")
        Integer yearMin,

        @Schema(description = "Maximum publication year, inclusive.", example = "2000")
        Integer yearMax
) {
}