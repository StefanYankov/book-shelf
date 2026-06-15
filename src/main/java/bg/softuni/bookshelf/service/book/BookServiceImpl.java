package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.data.entity.*;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.data.repository.*;
import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.ImageUploadService;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.UploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final LanguageRepository languageRepository;
    private final PublisherRepository publisherRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public BookDetailsDto createBook(BookCreateDto createDto, MultipartFile coverImageFile) {
        log.debug("Attempting to create new book");

        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);

        Author author = authorRepository.findById(createDto.authorId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
        Language language = languageRepository.findById(createDto.languageId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND));
        Publisher publisher = publisherRepository.findById(createDto.publisherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND));
        Set<Genre> genres = createDto.genreIds().stream()
                .map(genreId -> genreRepository.findById(genreId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.GENRE_NOT_FOUND)))
                .collect(Collectors.toSet());

        Book newBook = bookMapper.toBookEntity(createDto, author, language, publisher, genres);

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            UploadResult uploadResult = imageUploadService.uploadImage(coverImageFile);
            Image image = new Image();
            image.setUrl(uploadResult.url());
            image.setPublicId(uploadResult.publicId());
            newBook.setCoverImage(image);
        }

        Book savedBook = bookRepository.save(newBook);

        log.info("Successfully created new book with ID: {}", savedBook.getId());

        return bookMapper.toBookDetailsDto(savedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailsDto getById(UUID id) {
        log.debug("Fetching book by ID: {}", id);
        Book book = findBookOrThrow(id);
        return bookMapper.toBookDetailsDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> getAll(Pageable pageable) {
        log.debug("Fetching all books with pagination");
        Page<Book> bookPage = bookRepository.findAllWithAuthors(pageable);
        return bookPage.map(bookMapper::toBookSummaryDto);
    }

    @Override
    @Transactional
    public BookDetailsDto updateBook(UUID id, BookUpdateDto updateDto) {
        log.debug("Attempting to update shipment {}", id);
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);

        Book existingBook = findBookOrThrow(id);

        if (updateDto.title() != null) {
            existingBook.setTitle(updateDto.title());
        }
        if (updateDto.isbn() != null) {
            existingBook.setISBN(updateDto.isbn());
        }
        if (updateDto.pages() != null) {
            existingBook.setPages(updateDto.pages());
        }
        if (updateDto.yearPublished() != null) {
            existingBook.setYearPublished(updateDto.yearPublished());
        }
        if (updateDto.summary() != null) {
            existingBook.setSummary(updateDto.summary());
        }
        if (updateDto.format() != null) {
            existingBook.setFormat(updateDto.format());
        }

        if (updateDto.authorId() != null) {
            Author author = authorRepository.findById(updateDto.authorId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
            existingBook.setAuthor(author);
        }
        if (updateDto.languageId() != null) {
            Language language = languageRepository.findById(updateDto.languageId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND));
            existingBook.setLanguage(language);
        }
        if (updateDto.publisherId() != null) {
            Publisher publisher = publisherRepository.findById(updateDto.publisherId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND));
            existingBook.setPublisher(publisher);
        }
        if (updateDto.genreIds() != null) {
            Set<Genre> genres = updateDto.genreIds().stream()
                    .map(genreId -> genreRepository.findById(genreId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.GENRE_NOT_FOUND)))
                    .collect(Collectors.toSet());
            existingBook.setGenres(genres);
        }

        Book updatedBook = bookRepository.save(existingBook);

        log.info("Successfully updated book with ID: {}", updatedBook.getId());

        return bookMapper.toBookDetailsDto(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(UUID id) {
        log.debug("Attempting to delete a book with ID: {}", id);
        Objects.requireNonNull(id, DeveloperErrors.ENTITY_ID_NULL);

        Book bookToDelete = findBookOrThrow(id);
        bookRepository.delete(bookToDelete);

        log.info("Successfully deleted book with ID: {}", id);

    }

    /**
     * Centralized lookup and exception logic to DRY up the service methods.
     */
    private Book findBookOrThrow(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lookup failed. Company with ID [{}] not found.", id);
                    return new BusinessException(ErrorCode.BOOK_NOT_FOUND);
                });
    }
}
