package bg.softuni.bookshelf.service.review.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateDto(
        @Schema(description = "Review title.", example = "Even better on a reread")
        @NotBlank @Size(max = ValidationConstants.Review.MAX_TITLE_LENGTH)
        String title,

        @Schema(description = "Optional review body.",
                example = "The pacing lands more clearly the second time through.")
        @Size(max = ValidationConstants.Review.MAX_COMMENT_LENGTH)
        String comment,

        @Schema(description = "Rating from 1 to 5.", example = "4")
        @Min(ValidationConstants.Review.MIN_RATING) @Max(ValidationConstants.Review.MAX_RATING)
        Integer rating
) {}
