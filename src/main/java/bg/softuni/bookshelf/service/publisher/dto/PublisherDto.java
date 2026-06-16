package bg.softuni.bookshelf.service.publisher.dto;

import java.util.UUID;

public record PublisherDto(
        UUID id,
        String name
) {

}
