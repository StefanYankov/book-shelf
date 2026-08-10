package bg.softuni.bookshelf.service.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ReviewViewDto(
        @Schema(description = "Unique review identifier.",
                example = "aaaaaaaa-0000-0000-0000-000000000001")
        UUID id,

        @Schema(description = "Review title.", example = "A gripping classic")
        String title,

        @Schema(description = "Review body.",
                example = "Dense world-building and unforgettable characters.")
        String comment,

        @Schema(description = "Rating from 1 to 5.", example = "5")
        Integer rating,

        @Schema(description = "Identifier of the review's author.",
                example = "22222222-0000-0000-0000-000000000001")
        UUID userId,

        @Schema(description = "Display name of the review's author.", example = "jdoe")
        String username,

        @Schema(description = "Identifier of the reviewed target entity.",
                example = "66666666-0000-0000-0000-000000000001")
        UUID targetId,

        @Schema(description = "Type of the reviewed target entity.", example = "BOOK")
        String targetType,

        @Schema(description = "Timestamp when the review was created.",
                example = "2026-08-01T10:15:30Z")
        Instant createdAt,

        @Schema(description = "Timestamp when the review was last updated.",
                example = "2026-08-02T08:00:00Z")
        Instant updatedAt
) {}