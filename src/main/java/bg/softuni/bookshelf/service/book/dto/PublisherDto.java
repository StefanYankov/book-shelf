package bg.softuni.bookshelf.service.book.dto;

import java.util.UUID;

/**
 * A simple DTO representing a Publisher.
 */
public record PublisherDto(
        UUID id,
        String name
) {
}
