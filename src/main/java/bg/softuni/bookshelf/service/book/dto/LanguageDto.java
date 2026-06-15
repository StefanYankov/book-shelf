package bg.softuni.bookshelf.service.book.dto;

import java.util.UUID;

/**
 * A simple DTO representing a Language.
 */
public record LanguageDto(
        UUID id,
        String name
) {
}
