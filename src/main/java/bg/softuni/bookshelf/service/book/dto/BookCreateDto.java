package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.ISBN;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for creating a new Book.
 * Contains all necessary fields and validation constraints for user input.
 */
@Builder
public record BookCreateDto(
        @NotBlank(message = "{validation.book.title.notblank}")
        @Size(max = ValidationConstants.Book.MAX_TITLE_LENGTH, message = "{validation.book.title.toolong}")
        String title,

        @ISBN(message = "{validation.book.isbn.notvalid}")
        String isbn,

        @NotNull(message = "{validation.book.pages.notnull}")
        @Positive(message = "{validation.book.pages.positive}")
        Integer pages,

        @NotNull(message = "{validation.book.year.notnull}")
        Integer yearPublished,

        @NotBlank(message = "{validation.book.summary.notblank}")
        @Size(max = ValidationConstants.Book.MAX_SUMMARY_LENGTH, message = "{validation.book.summary.toolong}")
        String summary,

        @NotNull(message = "{validation.book.format.notnull}")
        BookFormat format,

        @NotNull(message = "{validation.book.author.notnull}")
        UUID authorId,

        @NotNull(message = "validation.book.language.notnull")
        UUID languageId,

        @NotNull(message = "{validation.book.publisher.notnull}")
        UUID publisherId,

        @NotEmpty(message = "{validation.book.genres.notempty}")
        Set<UUID> genreIds
) {
}
