package bg.softuni.bookshelf.service.author.dto;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO for returning detailed information about a single author,
 * including a paginated summary of their books.
 */
public record AuthorDetailsDto(
        @Schema(description = "Unique author identifier.",
                example = "55555555-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Author's full name.", example = "Frank Herbert")
        String name,

        @Schema(description = "Short biography of the author.",
                example = "American science-fiction author, best known for Dune.")
        String summary,

        @Schema(description = "URL of the author's image; null when none is set.",
                example = "https://res.cloudinary.com/demo/image/upload/herbert.jpg")
        String imageUrl,

        @Schema(description = "A page of books written by the author.")
        PagedResponse<BookSummaryDto> books
) {
}
