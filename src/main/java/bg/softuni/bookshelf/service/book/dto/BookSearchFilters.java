package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;

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
        String query,
        Set<String> genres,
        BookFormat format,
        Integer yearMin,
        Integer yearMax
) {
}
