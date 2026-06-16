package bg.softuni.bookshelf.service.publisher;

import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.repository.PublisherRepository;
import bg.softuni.bookshelf.service.publisher.dto.PublisherCreateDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherUpdateDto;
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
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    @Transactional
    public PublisherDto createPublisher(PublisherCreateDto createDto) {
        log.debug("Attempting to create a new publisher");

        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        if (publisherRepository.findByNameIgnoreCase(createDto.name().trim()).isPresent()) {
            log.warn("Publisher creation failed. Name [{}] already exists.", createDto.name());
            throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE);
        }

        Publisher newPublisher = publisherMapper.toEntity(createDto);

        Publisher savedPublisher = publisherRepository.save(newPublisher);

        log.info("Successfully created new publisher with ID: {}", savedPublisher.getId());

        return publisherMapper.toDto(savedPublisher);
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherDto getById(UUID id) {
        log.debug("Fetching publisher by ID: {}", id);
        Publisher publisher = findPublisherOrThrow(id);
        return publisherMapper.toDto(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublisherDto> getAll(Pageable pageable) {
        log.debug("Fetching all publishers with pagination");
        Page<Publisher> publisherPage = publisherRepository.findAll(pageable);
        return publisherPage.map(publisherMapper::toDto);
    }

    @Override
    @Transactional
    public PublisherDto updatePublisher(UUID id, PublisherUpdateDto updateDto) {
        log.debug("Attempting to update publisher with ID: {}", id);
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);

        Publisher publisherToUpdate = findPublisherOrThrow(id);

        if (updateDto.name() != null) {
            String newName = updateDto.name().trim();
            Optional<Publisher> existingLanguage = publisherRepository.findByNameIgnoreCase(newName);
            if (existingLanguage.isPresent() && !existingLanguage.get().getId().equals(publisherToUpdate.getId())) {
                log.warn("Publisher update failed. Name [{}] already exists for a different publisher.", newName);
                throw new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE);
            }
            publisherToUpdate.setName(newName);
        }

        Publisher updatedPublisher = publisherRepository.save(publisherToUpdate);

        log.info("Successfully updated publisher [{}] with ID: {}", updatedPublisher.getName(), id);

        return publisherMapper.toDto(updatedPublisher);
    }

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public void deletePublisher(UUID id) {
        log.debug("Attempting to delete a publisher with ID: {}", id);
        Objects.requireNonNull(id, DeveloperErrors.ENTITY_ID_NULL);

        Publisher publisherToDelete = findPublisherOrThrow(id);
        try {
            publisherRepository.delete(publisherToDelete);
            log.info("Successfully deleted publisher with ID: {}", id);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Failed to delete publisher {} due to data integrity violation: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.PUBLISHER_IN_USE);
        }
    }

    /**
     * Centralized lookup and exception logic to DRY up the service methods.
     */
    private Publisher findPublisherOrThrow(UUID id) {
        return publisherRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lookup failed. Publisher with ID [{}] not found.", id);
                    return new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND);
                });
    }
}
