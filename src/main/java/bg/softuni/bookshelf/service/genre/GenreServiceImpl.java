package bg.softuni.bookshelf.service.genre;

import bg.softuni.bookshelf.data.entity.Genre;
import bg.softuni.bookshelf.data.repository.GenreRepository;
import bg.softuni.bookshelf.service.genre.dto.GenreCreateDto;
import bg.softuni.bookshelf.service.genre.dto.GenreDto;
import bg.softuni.bookshelf.service.genre.dto.GenreUpdateDto;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public GenreDto createGenre(GenreCreateDto createDto) {
        log.debug("Attempting to create a new genre");

        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        if (genreRepository.findByNameIgnoreCase(createDto.name().trim()).isPresent()) {
            log.warn("Genre creation failed. Name [{}] already exists.", createDto.name());
            throw new BusinessException(ErrorCode.GENRE_NAME_DUPLICATE);
        }

        Genre newGenre = genreMapper.toEntity(createDto);

        Genre savedGenre = genreRepository.save(newGenre);

        log.info("Successfully created new genre with ID: {}", savedGenre.getId());

        return genreMapper.toDto(savedGenre);
    }

    @Override
    @Transactional(readOnly = true)
    public GenreDto getById(UUID id) {
        log.debug("Fetching genre by ID: {}", id);
        Genre genre = findGenreOrThrow(id);
        return genreMapper.toDto(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GenreDto> getAll(Pageable pageable) {
        log.debug("Fetching all genres with pagination");
        Page<Genre> languagePage = genreRepository.findAll(pageable);
        return languagePage.map(genreMapper::toDto);
    }

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public GenreDto updateGenre(UUID id, GenreUpdateDto updateDto) {
        log.debug("Attempting to update genre with ID: {}", id);
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);

        Genre genreToUpdate = findGenreOrThrow(id);

        if (updateDto.name() != null) {
            String newName = updateDto.name().trim();
            Optional<Genre> existingLanguage = genreRepository.findByNameIgnoreCase(newName);
            if (existingLanguage.isPresent() && !existingLanguage.get().getId().equals(genreToUpdate.getId())) {
                log.warn("Genre update failed. Name [{}] already exists for a different genre.", newName);
                throw new BusinessException(ErrorCode.GENRE_NAME_DUPLICATE);
            }
            genreToUpdate.setName(newName);
        }

        if (updateDto.description() != null) {
            genreToUpdate.setDescription(updateDto.description());
        }

        Genre updatedGenre = genreRepository.save(genreToUpdate);

        log.info("Successfully updated genre [{}] with ID: {}", updatedGenre.getName(), id);

        return genreMapper.toDto(updatedGenre);
    }

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public void deleteGenre(UUID id) {
        log.debug("Attempting to delete a genre with ID: {}", id);
        Objects.requireNonNull(id, DeveloperErrors.ENTITY_ID_NULL);

        Genre genreToDelete = findGenreOrThrow(id);
        try {
            genreRepository.delete(genreToDelete);
            log.info("Successfully deleted genre with ID: {}", id);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Failed to delete genre {} due to data integrity violation: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.GENRE_IN_USE);
        }
    }

    /**
     * Centralized lookup and exception logic to DRY up the service methods.
     */
    private Genre findGenreOrThrow(UUID id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lookup failed. Genre with ID [{}] not found.", id);
                    return new BusinessException(ErrorCode.GENRE_NOT_FOUND);
                });
    }
}
