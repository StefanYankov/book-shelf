package bg.softuni.bookshelf.service.bookshelf.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record BookshelfSummaryDto(
        UUID id,
        String name,
        int bookCount
) {

}
