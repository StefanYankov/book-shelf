package bg.softuni.bookshelf.service.author;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.data.repository.AuthorRepository;
import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.service.author.dto.AuthorUpdateDto;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.ImageUploadService;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final ImageUploadService imageUploadService;
    private final BookService bookService;

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public AuthorDetailsDto createAuthor(AuthorCreateDto createDto, MultipartFile imageFile) {
        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        log.debug("Attempting to create new author with name: {}", createDto.name());

        if (authorRepository.findByNameIgnoreCase(createDto.name().trim()).isPresent()) {
            throw new BusinessException(ErrorCode.AUTHOR_NAME_DUPLICATE);
        }

        Author newAuthor = authorMapper.toEntity(createDto);

        if (imageFile != null && !imageFile.isEmpty()) {
            UploadResult uploadResult = imageUploadService.uploadImage(imageFile);
            Image image = new Image();
            image.setUrl(uploadResult.url());
            image.setPublicId(uploadResult.publicId());
            newAuthor.setImage(image);
        }

        Author savedAuthor = authorRepository.save(newAuthor);
        log.info("Successfully created new author with ID: {}", savedAuthor.getId());

        return authorMapper.toDetailsDto(savedAuthor, Page.empty());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDetailsDto getById(UUID id) {
        log.debug("Fetching author by ID: {}", id);
        Author author = findAuthorOrThrow(id);
        // TODO: dynamicall fetch more than the first page of books.
        Page<BookSummaryDto> books = bookService.findAllByAuthor(id, PageRequest.of(0, 20));
        return authorMapper.toDetailsDto(author, books);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuthorSummaryDto> getAll(Pageable pageable) {
        log.debug("Fetching all authors with pagination");
        return authorRepository.findAll(pageable).map(authorMapper::toSummaryDto);
    }

    @Override
    @Transactional
    public AuthorDetailsDto updateAuthor(UUID id, AuthorUpdateDto updateDto) {
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);
        log.debug("Attempting to update author with ID: {}", id);

        Author authorToUpdate = findAuthorOrThrow(id);

        if (updateDto.name() != null) {
            String newName = updateDto.name().trim();
            Optional<Author> existingAuthor = authorRepository.findByNameIgnoreCase(newName);
            if (existingAuthor.isPresent() && !existingAuthor.get().getId().equals(authorToUpdate.getId())) {
                throw new BusinessException(ErrorCode.AUTHOR_NAME_DUPLICATE);
            }
            authorToUpdate.setName(newName);
        }

        if (updateDto.summary() != null) {
            authorToUpdate.setSummary(updateDto.summary());
        }

        Author updatedAuthor = authorRepository.save(authorToUpdate);
        log.info("Successfully updated author with ID: {}", updatedAuthor.getId());

        Page<BookSummaryDto> books = bookService.findAllByAuthor(id, PageRequest.of(0, 20));
        return authorMapper.toDetailsDto(updatedAuthor, books);
    }

    @Override
    @Transactional
    //TODO: add defence in depth @PreAuthorize("hasRole('ADMIN'))
    public void deleteAuthor(UUID id) {
        log.debug("Attempting to delete author with ID: {}", id);
        Author authorToDelete = findAuthorOrThrow(id);

        try {
            // This assumes that if an author has books, the DB will prevent deletion.
            authorRepository.delete(authorToDelete);

            if (authorToDelete.getImage() != null && authorToDelete.getImage().getPublicId() != null) {
                imageUploadService.deleteImage(authorToDelete.getImage().getPublicId());
            }
            log.info("Successfully deleted author with ID: {}", id);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Failed to delete author {} due to data integrity violation: {}", id, ex.getMessage());
            throw new BusinessException(ErrorCode.AUTHOR_IN_USE);
        }
    }

    /**
     * Centralized lookup and exception logic to DRY up the service methods.
     */
    private Author findAuthorOrThrow(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
    }
}
