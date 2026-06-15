package bg.softuni.bookshelf.service.book.dto;

import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

import java.util.Set;
import java.util.UUID;

/**
 * DTO for partially updating an existing Book.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
public record BookUpdateDto(

        @Size(max = ValidationConstants.Book.MAX_TITLE_LENGTH, message = "{validation.book.title.toolong}")
        String title,

        @ISBN(message = "{validation.book.isbn.notvalid}")
        String isbn,

        @Positive(message = "{validation.book.pages.positive}")
        Integer pages,

        Integer yearPublished,

        @Size(max = ValidationConstants.Book.MAX_SUMMARY_LENGTH, message = "{validation.book.summary.toolong}")
        String summary,

        BookFormat format,

        UUID authorId,

        UUID languageId,

        UUID publisherId,

        Set<UUID> genreIds
) {
}
