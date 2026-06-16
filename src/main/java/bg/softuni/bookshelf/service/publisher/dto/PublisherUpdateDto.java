package bg.softuni.bookshelf.service.publisher.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for partially updating an existing Publisher.
 * All fields are optional. Validation annotations are still applied
 * if a value is present.
 */
@Builder
public record PublisherUpdateDto(
        @Size(max = ValidationConstants.Publisher.MAX_NAME_LENGTH, message = "{validation.publisher.name.toolong}")
        String name
) {

}
