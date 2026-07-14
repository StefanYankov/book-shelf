package bg.softuni.bookshelf.service.review.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewViewDto(
        UUID id,
        String title,
        String comment,
        Integer rating,
        UUID userId,
        String username,
        UUID targetId,
        String targetType,
        Instant createdAt,
        Instant updatedAt
) {}
