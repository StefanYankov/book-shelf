package bg.softuni.bookshelf.service.review.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewUpdateDto(
        @NotBlank @Size(max = ValidationConstants.Review.MAX_TITLE_LENGTH)
        String title,

        @Size(max = ValidationConstants.Review.MAX_COMMENT_LENGTH)
        String comment,

        @Min(ValidationConstants.Review.MIN_RATING) @Max(ValidationConstants.Review.MAX_RATING)
        Integer rating
) {}
