package bg.softuni.bookshelf.service.genre;

import org.springframework.stereotype.Component;

import bg.softuni.bookshelf.data.entity.Genre;
import bg.softuni.bookshelf.service.genre.dto.GenreCreateDto;
import bg.softuni.bookshelf.service.genre.dto.GenreDto;

/**
 * Component responsible for mapping between Genre entities and their corresponding DTOs.
 */
@Component
public class GenreMapper {

    /**
     * Maps a {@link Genre} entity to a {@link GenreDto}.
     *
     * @param genre The persistent {@link Genre} entity.
     * @return A {@link GenreDto}.
     */
    public GenreDto toDto(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName(), genre.getDescription());
    }

    /**
     * Maps a {@link GenreCreateDto} to a new {@link Genre} entity.
     *
     * @param dto The source DTO containing the genre's creation data.
     * @return A new, transient {@link Genre} entity ready for persistence.
     */
    public Genre toEntity(GenreCreateDto dto) {
        Genre genre = new Genre();
        genre.setName(dto.name());
        return genre;
    }
}
