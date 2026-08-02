package bg.softuni.bookshelf.service.challenge.dto;

import bg.softuni.bookshelf.shared.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReadingChallengeCreateDto(

        @NotNull(message = "{validation.challenge.year.notnull}")
        @Min(value = ValidationConstants.Challenge.MIN_YEAR, message = "{validation.challenge.year.range}")
        @Max(value = ValidationConstants.Challenge.MAX_YEAR, message = "{validation.challenge.year.range}")
        Integer year,

        @NotNull(message = "{validation.challenge.goal.notnull}")
        @Min(value = ValidationConstants.Challenge.MIN_GOAL, message = "{validation.challenge.goal.range}")
        @Max(value = ValidationConstants.Challenge.MAX_GOAL, message = "{validation.challenge.goal.range}")
        Integer goal
) {

}