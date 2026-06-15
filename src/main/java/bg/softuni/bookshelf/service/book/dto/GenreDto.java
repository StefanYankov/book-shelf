package bg.softuni.bookshelf.service.book.dto;

import java.util.UUID;

/**
 * A simple DTO representing a Genre.
 */
public record GenreDto(
        UUID id,
        String name
) {
}
