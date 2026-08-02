package bg.softuni.bookshelf.service.challenge.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LogProgressDto(

        @NotNull(message = "{validation.challenge.progress.notnull}")
        @Min(value = ValidationConstants.Challenge.MIN_PROGRESS, message = "{validation.challenge.progress.range}")
        @Max(value = ValidationConstants.Challenge.MAX_PROGRESS, message = "{validation.challenge.progress.range}")
        Integer booksRead
) {

}