package bg.softuni.bookshelf.service.bookshelf.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record AddBookToBookshelfDto(
        @NotNull(message = "{validation.bookshelf.bookId.notnull}")
        UUID bookId
) {
}
