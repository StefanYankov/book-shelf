package bg.softuni.bookshelf.service.bookshelf.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record BookshelfUpdateDto(
        @NotBlank(message = "{validation.bookshelf.name.notblank}")
        @Size(
                min = ValidationConstants.Bookshelf.MIN_NAME_LENGTH,
                max = ValidationConstants.Bookshelf.MAX_NAME_LENGTH,
                message = "{validation.bookshelf.name.size}"
        )
        String name,

        @Size(
                max = ValidationConstants.Bookshelf.MAX_DESCRIPTION_LENGTH,
                message = "{validation.bookshelf.description.size}"
        )
        String description
) {
}
