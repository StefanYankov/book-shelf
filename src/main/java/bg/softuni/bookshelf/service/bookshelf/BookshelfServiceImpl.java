package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.BookshelfBook;
import bg.softuni.bookshelf.data.entity.BookshelfBookId;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.repository.BookRepository;
import bg.softuni.bookshelf.data.repository.BookshelfBookRepository;
import bg.softuni.bookshelf.data.repository.BookshelfRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.base.BaseService;
import bg.softuni.bookshelf.service.book.BookMapper;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.AddBookToBookshelfDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfCreateDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfUpdateDto;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookshelfServiceImpl extends BaseService implements BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookshelfBookRepository bookshelfBookRepository;
    private final BookshelfMapper bookshelfMapper;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookshelfDetailsDto createShelf(BookshelfCreateDto createDto, UUID ownerId) {
        Objects.requireNonNull(createDto, DeveloperErrors.DTO_NULL);
        log.debug("Creating new bookshelf for user {}", ownerId);

        User owner = findOrThrow(() -> userRepository.findById(ownerId), ErrorCode.USER_NOT_FOUND, ownerId);

        Bookshelf newShelf = new Bookshelf();
        newShelf.setName(createDto.name());
        newShelf.setDescription(createDto.description());
        newShelf.setUser(owner);

        Bookshelf savedShelf = bookshelfRepository.save(newShelf);
        log.info("Successfully created bookshelf {} for user {}", savedShelf.getId(), ownerId);

        return bookshelfMapper.toBookshelfDetailsDto(savedShelf);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookshelfSummaryDto> getShelvesForUser(UUID userId, Pageable pageable) {
        log.debug("Fetching shelves for user {}", userId);
        return bookshelfRepository.findAllByUser_Id(userId, pageable)
                .map(bookshelfMapper::toShelfSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookshelfDetailsDto getShelfById(UUID shelfId) {
        log.debug("Fetching details for bookshelf {}", shelfId);
        Bookshelf shelf = findOrThrow(() -> bookshelfRepository.findById(shelfId), ErrorCode.BOOKSHELF_NOT_FOUND, shelfId);
        return bookshelfMapper.toBookshelfDetailsDto(shelf);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookSummaryDto> getBooksInShelf(UUID shelfId, Pageable pageable) {
        log.debug("Fetching books in shelf {}", shelfId);

        if (!bookshelfRepository.existsById(shelfId)) {
            throw new BusinessException(ErrorCode.BOOKSHELF_NOT_FOUND, shelfId.toString());
        }

        Page<Book> books = bookshelfBookRepository.findBooksByBookshelfId(shelfId, pageable);
        return books.map(bookMapper::toBookSummaryDto);
    }

    @Override
    @Transactional
    public BookshelfDetailsDto updateShelf(UUID shelfId, BookshelfUpdateDto updateDto) {
        Objects.requireNonNull(updateDto, DeveloperErrors.DTO_NULL);
        log.debug("Updating bookshelf {}", shelfId);

        Bookshelf shelf = findOrThrow(() -> bookshelfRepository.findById(shelfId), ErrorCode.BOOKSHELF_NOT_FOUND, shelfId);
        shelf.setName(updateDto.name());
        shelf.setDescription(updateDto.description());

        Bookshelf updatedShelf = bookshelfRepository.save(shelf);
        log.info("Successfully updated bookshelf {}", shelfId);

        return bookshelfMapper.toBookshelfDetailsDto(updatedShelf);
    }

    @Override
    @Transactional
    public void deleteShelf(UUID shelfId) {
        log.debug("Deleting bookshelf {}", shelfId);
        Bookshelf shelf = findOrThrow(() -> bookshelfRepository.findById(shelfId), ErrorCode.BOOKSHELF_NOT_FOUND, shelfId);
        bookshelfRepository.delete(shelf);
        log.info("Successfully deleted bookshelf {}", shelfId);
    }

    @Override
    @Transactional
    public void addBookToShelf(UUID shelfId, AddBookToBookshelfDto addBookDto) {
        Objects.requireNonNull(addBookDto, DeveloperErrors.DTO_NULL);
        log.debug("Adding book {} to shelf {}", addBookDto.bookId(), shelfId);

        Bookshelf shelf = findOrThrow(() -> bookshelfRepository.findById(shelfId), ErrorCode.BOOKSHELF_NOT_FOUND, shelfId);
        Book book = findOrThrow(() -> bookRepository.findById(addBookDto.bookId()), ErrorCode.BOOK_NOT_FOUND, addBookDto.bookId());

        BookshelfBookId id = new BookshelfBookId();
        id.setBookshelfId(shelfId);
        id.setBookId(addBookDto.bookId());

        if (bookshelfBookRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_IN_SHELF);
        }

        BookshelfBook newEntry = new BookshelfBook();
        newEntry.setId(id);
        newEntry.setBookshelf(shelf);
        newEntry.setBook(book);

        bookshelfBookRepository.save(newEntry);
        log.info("Successfully added book {} to shelf {}", addBookDto.bookId(), shelfId);
    }

    @Override
    @Transactional
    public void removeBookFromShelf(UUID shelfId, UUID bookId) {
        log.debug("Removing book {} from shelf {}", bookId, shelfId);

        BookshelfBookId id = new BookshelfBookId();
        id.setBookshelfId(shelfId);
        id.setBookId(bookId);

        BookshelfBook entry = findOrThrow(() -> bookshelfBookRepository.findById(id), ErrorCode.BOOK_NOT_IN_SHELF, id);
        bookshelfBookRepository.delete(entry);
        log.info("Successfully removed book {} from shelf {}", bookId, shelfId);
    }
}
