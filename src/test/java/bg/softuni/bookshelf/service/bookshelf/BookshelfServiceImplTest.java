package bg.softuni.bookshelf.service.bookshelf;

import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Bookshelf;
import bg.softuni.bookshelf.data.entity.BookshelfBook;
import bg.softuni.bookshelf.data.entity.BookshelfBookId;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.repository.BookRepository;
import bg.softuni.bookshelf.data.repository.BookshelfBookRepository;
import bg.softuni.bookshelf.data.repository.BookshelfRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.book.BookMapper;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.AddBookToBookshelfDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfCreateDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfUpdateDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookshelfServiceImplTest {

    @Mock
    private BookshelfRepository bookshelfRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookshelfBookRepository bookshelfBookRepository;
    @Mock
    private BookshelfMapper bookshelfMapper;
    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookshelfServiceImpl bookshelfService;

    private ApplicationUser testUser;
    private Book testBook;
    private Bookshelf testShelf;

    @BeforeEach
    void setUp() {
        testUser = new ApplicationUser();
        testUser.setId(UUID.randomUUID());

        testBook = new Book();
        testBook.setId(UUID.randomUUID());

        testShelf = new Bookshelf();
        testShelf.setId(UUID.randomUUID());
        testShelf.setUser(testUser);
    }

    @Nested
    @DisplayName("createShelf Tests")
    class CreateShelfTests {
        @Test
        @DisplayName("Happy Path: Should create and save a new shelf")
        void shouldCreateAndSaveShelf() {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("My Shelf").description("A description").build();
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(bookshelfRepository.save(any(Bookshelf.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(bookshelfMapper.toBookshelfDetailsDto(any(Bookshelf.class))).thenAnswer(invocation -> {
                Bookshelf saved = invocation.getArgument(0);
                return BookshelfDetailsDto.builder().id(saved.getId()).name(saved.getName()).description(saved.getDescription()).build();
            });

            // Act
            BookshelfDetailsDto result = bookshelfService.createShelf(createDto, testUser.getId());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("My Shelf");
            verify(bookshelfRepository).save(any(Bookshelf.class));
        }

        @Test
        @DisplayName("Error Case: Should throw exception if owner not found")
        void shouldThrowExceptionWhenOwnerNotFound() {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("My Shelf").build();
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.createShelf(createDto, testUser.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getShelvesForUser Tests")
    class GetShelvesForUserTests {
        @Test
        @DisplayName("Happy Path: Should return paginated shelves for user")
        void shouldReturnShelves() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Bookshelf> shelfPage = new PageImpl<>(List.of(testShelf));
            when(bookshelfRepository.findAllByUser_Id(testUser.getId(), pageable)).thenReturn(shelfPage);
            when(bookshelfMapper.toShelfSummaryDto(any(Bookshelf.class))).thenReturn(BookshelfSummaryDto.builder().build());

            // Act
            Page<BookshelfSummaryDto> result = bookshelfService.getShelvesForUser(testUser.getId(), pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(bookshelfRepository).findAllByUser_Id(testUser.getId(), pageable);
        }
    }

    @Nested
    @DisplayName("getShelfById Tests")
    class GetShelfByIdTests {
        @Test
        @DisplayName("Happy Path: Should return shelf details")
        void shouldReturnShelfDetails() {
            // Arrange
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            when(bookshelfMapper.toBookshelfDetailsDto(testShelf)).thenReturn(BookshelfDetailsDto.builder().id(testShelf.getId()).name("Test").build());

            // Act
            BookshelfDetailsDto result = bookshelfService.getShelfById(testShelf.getId());

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testShelf.getId());
        }

        @Test
        @DisplayName("Error Case: Should throw when shelf not found")
        void shouldThrowWhenShelfNotFound() {
            // Arrange
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.getShelfById(testShelf.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKSHELF_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("getBooksInShelf Tests")
    class GetBooksInShelfTests {
        @Test
        @DisplayName("Happy Path: Should return paginated books in shelf")
        void shouldReturnBooksInShelf() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(testBook));
            when(bookshelfRepository.existsById(testShelf.getId())).thenReturn(true);
            when(bookshelfBookRepository.findBooksByBookshelfId(testShelf.getId(), pageable)).thenReturn(bookPage);
            when(bookMapper.toBookSummaryDto(testBook)).thenReturn(BookSummaryDto.builder().id(testBook.getId()).build());

            // Act
            Page<BookSummaryDto> result = bookshelfService.getBooksInShelf(testShelf.getId(), pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().id()).isEqualTo(testBook.getId());
        }

        @Test
        @DisplayName("Error Case: Should throw when shelf not found")
        void shouldThrowWhenShelfNotFound() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            when(bookshelfRepository.existsById(testShelf.getId())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.getBooksInShelf(testShelf.getId(), pageable))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKSHELF_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateShelf Tests")
    class UpdateShelfTests {
        @Test
        @DisplayName("Happy Path: Should update shelf details")
        void shouldUpdateShelf() {
            // Arrange
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Updated Name").description("Updated Desc").build();
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            when(bookshelfRepository.save(any(Bookshelf.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(bookshelfMapper.toBookshelfDetailsDto(any(Bookshelf.class))).thenAnswer(invocation -> {
                Bookshelf saved = invocation.getArgument(0);
                return BookshelfDetailsDto.builder().id(saved.getId()).name(saved.getName()).description(saved.getDescription()).build();
            });

            // Act
            BookshelfDetailsDto result = bookshelfService.updateShelf(testShelf.getId(), updateDto);

            // Assert
            assertThat(result.name()).isEqualTo("Updated Name");
            assertThat(result.description()).isEqualTo("Updated Desc");
            verify(bookshelfRepository).save(testShelf);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if shelf not found")
        void shouldThrowWhenShelfNotFound() {
            // Arrange
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Updated Name").build();
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.updateShelf(testShelf.getId(), updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKSHELF_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteShelf Tests")
    class DeleteShelfTests {
        @Test
        @DisplayName("Happy Path: Should delete the shelf")
        void shouldDeleteShelf() {
            // Arrange
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            doNothing().when(bookshelfRepository).delete(testShelf);

            // Act
            bookshelfService.deleteShelf(testShelf.getId());

            // Assert
            verify(bookshelfRepository).delete(testShelf);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if shelf not found")
        void shouldThrowWhenShelfNotFound() {
            // Arrange
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.deleteShelf(testShelf.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKSHELF_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("addBookToShelf Tests")
    class AddBookToShelfTests {
        @Test
        @DisplayName("Happy Path: Should add a book to a shelf")
        void shouldAddBookToShelf() {
            // Arrange
            AddBookToBookshelfDto addDto = AddBookToBookshelfDto.builder().bookId(testBook.getId()).build();
            BookshelfBookId id = new BookshelfBookId();
            id.setBookshelfId(testShelf.getId());
            id.setBookId(testBook.getId());

            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));
            when(bookshelfBookRepository.existsById(id)).thenReturn(false);

            // Act
            bookshelfService.addBookToShelf(testShelf.getId(), addDto);

            // Assert
            verify(bookshelfBookRepository).save(any(BookshelfBook.class));
        }

        @Test
        @DisplayName("Error Case: Should throw exception if book is already in shelf")
        void shouldThrowExceptionIfBookAlreadyInShelf() {
            // Arrange
            AddBookToBookshelfDto addDto = AddBookToBookshelfDto.builder().bookId(testBook.getId()).build();
            BookshelfBookId id = new BookshelfBookId();
            id.setBookshelfId(testShelf.getId());
            id.setBookId(testBook.getId());

            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.of(testBook));
            when(bookshelfBookRepository.existsById(id)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.addBookToShelf(testShelf.getId(), addDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_ALREADY_IN_SHELF);
        }

        @Test
        @DisplayName("Error Case: Should throw if shelf not found")
        void shouldThrowWhenShelfNotFound() {
            // Arrange
            AddBookToBookshelfDto addDto = AddBookToBookshelfDto.builder().bookId(testBook.getId()).build();
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.addBookToShelf(testShelf.getId(), addDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOKSHELF_NOT_FOUND);
            verify(bookRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Error Case: Should throw if book not found")
        void shouldThrowWhenBookNotFound() {
            // Arrange
            AddBookToBookshelfDto addDto = AddBookToBookshelfDto.builder().bookId(testBook.getId()).build();
            when(bookshelfRepository.findById(testShelf.getId())).thenReturn(Optional.of(testShelf));
            when(bookRepository.findById(testBook.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.addBookToShelf(testShelf.getId(), addDto))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
        }
    }
    
    @Nested
    @DisplayName("removeBookFromShelf Tests")
    class RemoveBookFromShelfTests {

        @Test
        @DisplayName("Happy Path: Should remove a book from a shelf")
        void shouldRemoveBookFromShelf() {
            // Arrange
            BookshelfBookId id = new BookshelfBookId();
            id.setBookshelfId(testShelf.getId());
            id.setBookId(testBook.getId());
            
            BookshelfBook entry = new BookshelfBook();
            entry.setId(id);
            
            when(bookshelfBookRepository.findById(id)).thenReturn(Optional.of(entry));

            // Act
            bookshelfService.removeBookFromShelf(testShelf.getId(), testBook.getId());

            // Assert
            verify(bookshelfBookRepository).delete(entry);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if book is not in shelf")
        void shouldThrowExceptionIfBookNotInShelf() {
            // Arrange
            BookshelfBookId id = new BookshelfBookId();
            id.setBookshelfId(testShelf.getId());
            id.setBookId(testBook.getId());
            
            when(bookshelfBookRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookshelfService.removeBookFromShelf(testShelf.getId(), testBook.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_IN_SHELF);
        }
    }
}
