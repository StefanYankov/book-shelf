package bg.softuni.bookshelf.service.genre.dto;

import java.util.UUID;

public record GenreDto(
        UUID id,
        String name,
        String description
) {}
