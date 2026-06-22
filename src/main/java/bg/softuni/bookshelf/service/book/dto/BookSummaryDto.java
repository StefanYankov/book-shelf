package bg.softuni.bookshelf.service.book.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * DTO for returning a summary of a book, suitable for lists.
 */
@Builder
public record BookSummaryDto(
        UUID id,
        String title,
        String authorName,
        String coverImageUrl
) {
}
