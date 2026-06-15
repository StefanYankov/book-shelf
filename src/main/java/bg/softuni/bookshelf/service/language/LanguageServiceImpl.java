package bg.softuni.bookshelf.service.language;

import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.repository.LanguageRepository;
import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import bg.softuni.bookshelf.service.language.dto.LanguageUpdateDto;
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
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;
    private final LanguageMapper languageMapper;

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public LanguageDto createLanguage(LanguageCreateDto createDto) {
        log.debug("Attempting to create a new language");

        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        if (languageRepository.findByNameIgnoreCase(createDto.name().trim()).isPresent()) {
            log.warn("Language creation failed. Name [{}] already exists.", createDto.name());
            throw new BusinessException(ErrorCode.LANGUAGE_NAME_DUPLICATE);
        }

        Language newLanguage = languageMapper.toEntity(createDto);

        Language savedLanguage = languageRepository.save(newLanguage);

                log.info("Successfully created new language with ID: {}", savedLanguage.getId());

        return languageMapper.toDto(savedLanguage);
    }

    @Override
    @Transactional(readOnly = true)
    public LanguageDto getById(UUID id) {
        log.debug("Fetching language by ID: {}", id);
        Language language = findLanguageOrThrow(id);
        return languageMapper.toDto(language);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LanguageDto> getAll(Pageable pageable) {
        log.debug("Fetching all languages with pagination");
        Page<Language> languagePage = languageRepository.findAll(pageable);
        return languagePage.map(languageMapper::toDto);
    }

    @Override
    @Transactional
    public LanguageDto updateLanguage(UUID id, LanguageUpdateDto updateDto) {
        log.debug("Attempting to update language with ID: {}", id);
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);

        Language languageToUpdate = findLanguageOrThrow(id);
        String newName = updateDto.name().trim();

        Optional<Language> existingLanguage = languageRepository.findByNameIgnoreCase(newName);
        if (existingLanguage.isPresent() && !existingLanguage.get().getId().equals(languageToUpdate.getId())) {
            log.warn("Language update failed. Name [{}] already exists for a different language.", newName);
            throw new BusinessException(ErrorCode.LANGUAGE_NAME_DUPLICATE);
        }

        languageToUpdate.setName(newName);

        Language updatedLanguage = languageRepository.save(languageToUpdate);

        log.info("Successfully updated language [{}] with ID: {}", updatedLanguage.getName(), id);

        return languageMapper.toDto(updatedLanguage);
    }

    @Override
    @Transactional
    public void deleteLanguage(UUID id) {
        log.debug("Attempting to delete a language with ID: {}", id);
        Objects.requireNonNull(id, DeveloperErrors.ENTITY_ID_NULL);

        Language languageToDelete = findLanguageOrThrow(id);
        try {
            languageRepository.delete(languageToDelete);
            log.info("Successfully deleted language with ID: {}", id);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Failed to delete language {} due to data integrity violation: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.LANGUAGE_IN_USE);
        }
    }

    /**
     * Centralized lookup and exception logic to DRY up the service methods.
     */
    private Language findLanguageOrThrow(UUID id) {
        return languageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lookup failed. Language with ID [{}] not found.", id);
                    return new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND);
                });
    }
}
