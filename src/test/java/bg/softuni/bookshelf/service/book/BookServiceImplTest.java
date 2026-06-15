package bg.softuni.bookshelf.service.book;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.Book;
import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.repository.*;
import bg.softuni.bookshelf.service.book.dto.BookCreateDto;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
            UploadResult mockUploadResult = new UploadResult("http://example.com/image.jpg", "public-id");
            Book mappedBook = new Book();
            Book savedBook = new Book();
            BookDetailsDto expectedDto = new BookDetailsDto(UUID.randomUUID(), "The Hobbit", null, 0, 0, null, null, null, null, null, null, null);

            given(authorRepository.findById(dto.authorId())).willReturn(Optional.of(new Author()));
            given(languageRepository.findById(dto.languageId())).willReturn(Optional.of(new Language()));
            given(publisherRepository.findById(dto.publisherId())).willReturn(Optional.of(new Publisher()));
            dto.genreIds().forEach(id -> given(genreRepository.findById(id)).willReturn(Optional.of(new bg.softuni.bookshelf.data.entity.Genre())));
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
            assertThat(capturedBook.getCoverImage().getUrl()).isEqualTo("http://example.com/image.jpg");
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
            dto.genreIds().forEach(id -> given(genreRepository.findById(id)).willReturn(Optional.of(new bg.softuni.bookshelf.data.entity.Genre())));
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
}
