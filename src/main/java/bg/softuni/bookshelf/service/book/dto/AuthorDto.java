package bg.softuni.bookshelf.service.book.dto;

import java.util.UUID;

/**
 * A simple DTO representing an Author's basic information.
 */
public record AuthorDto(
        UUID id,
        String name
) {
}
