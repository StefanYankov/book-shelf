package bg.softuni.bookshelf.service.author.dto;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * DTO for returning detailed information about a single author,
 * including a paginated summary of their books.
 */
public record AuthorDetailsDto(
        UUID id,
        String name,
        String summary,
        String imageUrl,
        Page<BookSummaryDto> books
) {
}
