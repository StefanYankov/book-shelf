package bg.softuni.bookshelf.service.publisher;

import org.springframework.stereotype.Component;

import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.service.publisher.dto.*;

/**
 * Component responsible for mapping between {@link Publisher} entities and their corresponding DTOs.
 */
@Component
public class PublisherMapper {

    /**
     * Maps a {@link Publisher} entity to a {@link PublisherDto}.
     *
     * @param publisher The persistent {@link Publisher} entity.
     * @return A {@link PublisherDto}.
     */
    public PublisherDto toDto(Publisher publisher) {
        return new PublisherDto(publisher.getId(), publisher.getName());
    }

    /**
     * Maps a {@link PublisherCreateDto} to a new {@link Publisher} entity.
     *
     * @param dto The source DTO containing the publisher's creation data.
     * @return A new, transient {@link Publisher} entity ready for persistence.
     */
    public Publisher toEntity(PublisherCreateDto dto) {
        Publisher publisher = new Publisher();
        publisher.setName(dto.name());
        return publisher;
    }
}
