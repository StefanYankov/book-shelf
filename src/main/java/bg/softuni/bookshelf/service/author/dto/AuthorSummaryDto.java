package bg.softuni.bookshelf.service.author.dto;

import java.util.UUID;

/**
 * DTO for returning a summary of an author, suitable for lists.
 */
public record AuthorSummaryDto(
        UUID id,
        String name,
        String imageUrl
) {
}
