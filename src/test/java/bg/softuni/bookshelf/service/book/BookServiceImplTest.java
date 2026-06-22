package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Genre;
import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.repository.*;
import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.ImageUploadService;
import bg.softuni.bookshelf.shared.infrastructure.filestorage.image.UploadResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private BookServiceImpl bookServiceImpl;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    // --- TEST DATA FACTORY ---
    private BookCreateDto.BookCreateDtoBuilder createValidDtoBuilder() {
        return BookCreateDto.builder()
                .title("The Hobbit")
                .authorId(UUID.randomUUID())
                .languageId(UUID.randomUUID())
                .publisherId(UUID.randomUUID())
                .genreIds(Set.of(UUID.randomUUID()));
    }

    @Nested
    @DisplayName("createBook(BookCreateDto, MultipartFile) Tests")
    class CreateBookTests {

        @Test
        @DisplayName("Defense in Depth: Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.createBook(null, null))
                    .isInstanceOf(NullPointerException.class);

            verifyNoInteractions(bookRepository, imageUploadService);
        }

        @Test
        @DisplayName("Happy Path: Should create book successfully with image upload")
        void shouldCreateBookWithImage() {
            // Arrange
            BookCreateDto dto = createValidDtoBuilder().build();
            MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});
            UploadResult mockUploadResult = new UploadResult("https://example.com/image.jpg", "public-id");
            Book mappedBook = new Book();
            Book savedBook = new Book();
            BookDetailsDto expectedDto = new BookDetailsDto(UUID.randomUUID(), "The Hobbit", null, 0, 0, null, null, null, null, null, null, null);

            given(authorRepository.findById(dto.authorId())).willReturn(Optional.of(new Author()));
            given(languageRepository.findById(dto.languageId())).willReturn(Optional.of(new Language()));
            given(publisherRepository.findById(dto.publisherId())).willReturn(Optional.of(new Publisher()));
            dto.genreIds().forEach(id -> given(genreRepository.findById(id)).willReturn(Optional.of(new Genre())));
            given(imageUploadService.uploadImage(imageFile)).willReturn(mockUploadResult);
            given(bookMapper.toBookEntity(any(), any(), any(), any(), any())).willReturn(mappedBook);
            given(bookRepository.save(mappedBook)).willReturn(savedBook);
            given(bookMapper.toBookDetailsDto(savedBook)).willReturn(expectedDto);

            // Act
            BookDetailsDto result = bookServiceImpl.createBook(dto, imageFile);

            // Assert
            assertThat(result).isEqualTo(expectedDto);

            verify(bookRepository).save(bookCaptor.capture());
            Book capturedBook = bookCaptor.getValue();
            assertThat(capturedBook.getCoverImage()).isNotNull();
            assertThat(capturedBook.getCoverImage().getUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(capturedBook.getCoverImage().getPublicId()).isEqualTo("public-id");
        }

        @Test
        @DisplayName("Happy Path: Should create book successfully without an image")
        void shouldCreateBookWithoutImage() {
            // Arrange
            BookCreateDto dto = createValidDtoBuilder().build();
            Book mappedBook = new Book();
            Book savedBook = new Book();
            BookDetailsDto expectedDto = new BookDetailsDto(UUID.randomUUID(), "The Hobbit", null, 0, 0, null, null, null, null, null, null, null);

            given(authorRepository.findById(dto.authorId())).willReturn(Optional.of(new Author()));
            given(languageRepository.findById(dto.languageId())).willReturn(Optional.of(new Language()));
            given(publisherRepository.findById(dto.publisherId())).willReturn(Optional.of(new Publisher()));
            dto.genreIds().forEach(id -> given(genreRepository.findById(id)).willReturn(Optional.of(new Genre())));
            given(bookMapper.toBookEntity(any(), any(), any(), any(), any())).willReturn(mappedBook);
            given(bookRepository.save(mappedBook)).willReturn(savedBook);
            given(bookMapper.toBookDetailsDto(savedBook)).willReturn(expectedDto);

            // Act
            BookDetailsDto result = bookServiceImpl.createBook(dto, null);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            verifyNoInteractions(imageUploadService);
            verify(bookRepository).save(bookCaptor.capture());
            assertThat(bookCaptor.getValue().getCoverImage()).isNull();
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when author is not found")
        void shouldThrowExceptionWhenAuthorNotFound() {
            // Arrange
            BookCreateDto dto = createValidDtoBuilder().build();
            given(authorRepository.findById(dto.authorId())).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.createBook(dto, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);

            verify(bookRepository, never()).save(any());
            verifyNoInteractions(imageUploadService, bookMapper);
        }
    }

    @Nested
    @DisplayName("Read Operation Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("getById: Should return DTO when book is found")
        void getById_shouldReturnDtoWhenBookFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            Book mockBook = new Book();
            BookDetailsDto expectedDto = new BookDetailsDto(bookId, "Found Book", null, 0, 0, null, null, null, null, null, null, null);

            given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.of(mockBook));
            given(bookMapper.toBookDetailsDto(mockBook)).willReturn(expectedDto);

            // Act
            BookDetailsDto result = bookServiceImpl.getById(bookId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("getById: Should throw BusinessException when book is not found")
        void getById_shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            given(bookRepository.findBookDetailsById(bookId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.getById(bookId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BOOK_NOT_FOUND);

            verifyNoInteractions(bookMapper);
        }

        @Test
        @DisplayName("getAll: Should return paginated DTOs")
        void getAll_shouldReturnPaginatedDtos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Book mockBook = new Book();
            Page<Book> bookPage = new PageImpl<>(List.of(mockBook), pageable, 1);
            BookSummaryDto summaryDto = new BookSummaryDto(UUID.randomUUID(), "Summary", null, null);

            given(bookRepository.findAllWithAuthors(pageable)).willReturn(bookPage);
            given(bookMapper.toBookSummaryDto(mockBook)).willReturn(summaryDto);

            // Act
            Page<BookSummaryDto> result = bookServiceImpl.getAll(pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst()).isEqualTo(summaryDto);
        }

        @Test
        @DisplayName("findAllByAuthor: Should return correct paginated DTOs")
        void findAllByAuthor_shouldReturnCorrectDtos() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Book mockBook = new Book();
            Page<Book> bookPage = new PageImpl<>(List.of(mockBook), pageable, 1);
            BookSummaryDto summaryDto = new BookSummaryDto(UUID.randomUUID(), "Book by Author", null, null);

            given(bookRepository.findAllByAuthorId(authorId, pageable)).willReturn(bookPage);
            given(bookMapper.toBookSummaryDto(mockBook)).willReturn(summaryDto);

            // Act
            Page<BookSummaryDto> result = bookServiceImpl.findAllByAuthor(authorId, pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst()).isEqualTo(summaryDto);
        }
    }

    @Nested
    @DisplayName("updateBook(UUID, BookUpdateDto) Tests")
    class UpdateBookTests {

        @Test
        @DisplayName("Defense in Depth: Should throw NullPointerException when DTO is null")
        void shouldThrowExceptionWhenDtoIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.updateBook(UUID.randomUUID(), null))
                    .isInstanceOf(NullPointerException.class);
            verifyNoInteractions(bookRepository);
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().title("New Title").build();
            given(bookRepository.findById(bookId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.updateBook(bookId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        }

        @Test
        @DisplayName("Happy Path: Should update only the title")
        void shouldUpdateOnlyTitle() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().title("New Title").build();
            Book existingBook = new Book();
            existingBook.setTitle("Old Title");
            existingBook.setSummary("Old Summary");

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(bookRepository.save(any(Book.class))).willReturn(existingBook);
            given(bookMapper.toBookDetailsDto(any(Book.class))).willReturn(mock(BookDetailsDto.class));

            // Act
            bookServiceImpl.updateBook(bookId, updateDto);

            // Assert
            verify(bookRepository).save(bookCaptor.capture());
            Book capturedBook = bookCaptor.getValue();
            assertThat(capturedBook.getTitle()).isEqualTo("New Title");
            assertThat(capturedBook.getSummary()).isEqualTo("Old Summary");
        }

        @Test
        @DisplayName("Happy Path: Should update relational author")
        void shouldUpdateRelationalAuthor() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            UUID newAuthorId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().authorId(newAuthorId).build();

            Book existingBook = new Book();
            Author newAuthor = new Author();
            newAuthor.setId(newAuthorId);

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(authorRepository.findById(newAuthorId)).willReturn(Optional.of(newAuthor));
            given(bookRepository.save(any(Book.class))).willReturn(existingBook);
            given(bookMapper.toBookDetailsDto(any(Book.class))).willReturn(mock(BookDetailsDto.class));

            // Act
            bookServiceImpl.updateBook(bookId, updateDto);

            // Assert
            verify(bookRepository).save(bookCaptor.capture());
            assertThat(bookCaptor.getValue().getAuthor().getId()).isEqualTo(newAuthorId);
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when new author is not found")
        void shouldThrowExceptionWhenNewAuthorNotFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            UUID newAuthorId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().authorId(newAuthorId).build();
            Book existingBook = new Book();

            given(bookRepository.findById(bookId)).willReturn(Optional.of(existingBook));
            given(authorRepository.findById(newAuthorId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.updateBook(bookId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);

            verify(bookRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteBook(UUID) Tests")
    class DeleteBookTests {

        @Test
        @DisplayName("Happy Path: Should delete book when found")
        void shouldDeleteBookWhenFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            given(bookRepository.findById(bookId)).willReturn(Optional.of(new Book()));

            // Act
            bookServiceImpl.deleteBook(bookId);

            // Assert
            verify(bookRepository).delete(any(Book.class));
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when book not found")
        void shouldThrowExceptionOnDeleteWhenBookNotFound() {
            // Arrange
            UUID bookId = UUID.randomUUID();
            given(bookRepository.findById(bookId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookServiceImpl.deleteBook(bookId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BOOK_NOT_FOUND);

            verify(bookRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("searchBooks(String, Pageable)")
    class SearchBooksTests {

        @Test
        @DisplayName("Search: Should call findAll when query is null")
        void shouldCallFindAllWhenQueryIsNull() {
            // Arrange
            String query = null;
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> mockBookPage = new PageImpl<>(List.of(new Book()));
            BookSummaryDto mockDto = new BookSummaryDto(UUID.randomUUID(), "Title", "Author", "http://example.com/image.jpg");

            when(bookRepository.findAllWithAuthors(any(Pageable.class))).thenReturn(mockBookPage);
            when(bookMapper.toBookSummaryDto(any(Book.class))).thenReturn(mockDto);

            // Act
            Page<BookSummaryDto> result = bookServiceImpl.searchBooks(query, pageable);

            // Assert
            assertNotNull(result);
            verify(bookRepository).findAllWithAuthors(pageable);
            verify(bookRepository, never()).searchByTitleOrAuthor(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Search: Should call findAll when query is empty string")
        void shouldCallFindAllWhenQueryIsEmpty() {
            // Arrange
            String query = "";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> mockBookPage = new PageImpl<>(List.of(new Book()));
            BookSummaryDto mockDto = new BookSummaryDto(UUID.randomUUID(), "Title", "Author", "http://example.com/image.jpg");

            when(bookRepository.findAllWithAuthors(any(Pageable.class))).thenReturn(mockBookPage);
            when(bookMapper.toBookSummaryDto(any(Book.class))).thenReturn(mockDto);

            // Act
            Page<BookSummaryDto> result = bookServiceImpl.searchBooks(query, pageable);

            // Assert
            assertNotNull(result);
            verify(bookRepository).findAllWithAuthors(pageable);
            verify(bookRepository, never()).searchByTitleOrAuthor(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Search: Should call findAll when query is only whitespace")
        void shouldCallFindAllWhenQueryIsBlank() {
            // Arrange
            String query = "   ";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> mockBookPage = new PageImpl<>(List.of(new Book()));
            BookSummaryDto mockDto = new BookSummaryDto(UUID.randomUUID(), "Title", "Author", "http://example.com/image.jpg");

            when(bookRepository.findAllWithAuthors(any(Pageable.class))).thenReturn(mockBookPage);
            when(bookMapper.toBookSummaryDto(any(Book.class))).thenReturn(mockDto);

            // Act
            Page<BookSummaryDto> result = bookServiceImpl.searchBooks(query, pageable);

            // Assert
            assertNotNull(result);
            verify(bookRepository).findAllWithAuthors(pageable);
            verify(bookRepository, never()).searchByTitleOrAuthor(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Search: Should call searchByTitleOrAuthor when query is a valid string")
        void shouldCallSearchWhenQueryIsValid() {
            // Arrange
            String query = "Tolkien";
            Pageable p = PageRequest.of(0, 10);
            given(bookRepository.searchByTitleOrAuthor(query, p)).willReturn(Page.empty());

            // Act
            bookServiceImpl.searchBooks(query, p);

            // Assert
            verify(bookRepository).searchByTitleOrAuthor(query, p);
            verify(bookRepository, never()).findAll(any(Pageable.class));
        }
    }
}
