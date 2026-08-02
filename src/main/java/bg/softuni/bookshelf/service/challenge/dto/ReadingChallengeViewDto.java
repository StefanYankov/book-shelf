package bg.softuni.bookshelf.service.challenge.dto;

import java.util.UUID;

public record ReadingChallengeViewDto(
        UUID id,
        UUID userId,
        int year,
        int goal,
        int booksRead,
        boolean completed
) {

}