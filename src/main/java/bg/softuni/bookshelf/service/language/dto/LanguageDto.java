package bg.softuni.bookshelf.service.language.dto;

import java.util.UUID;

/**
 * DTO for returning Language information.
 */
public record LanguageDto(
        UUID id,
        String name
) {
}
