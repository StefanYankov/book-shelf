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
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
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
        return null;
    }

    @Override
    public void deleteBook(UUID id) {

    }
}
