package bg.softuni.bookshelf.service.author;

import bg.softuni.bookshelf.data.entity.Author;
import bg.softuni.bookshelf.data.entity.value.Image;
import bg.softuni.bookshelf.data.repository.AuthorRepository;
import bg.softuni.bookshelf.service.author.dto.AuthorCreateDto;
import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorUpdateDto;
import bg.softuni.bookshelf.service.book.BookService;
import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorService Unit Tests")
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @Mock
    private ImageUploadService imageUploadService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private AuthorServiceImpl authorService;

    @Captor
    private ArgumentCaptor<Author> authorCaptor;

    // --- DATA FACTORIES ---
    private Author createMockAuthor(UUID id, String name) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        return author;
    }

    @Nested
    @DisplayName("createAuthor Tests")
    class CreateAuthorTests {

        @Test
        @DisplayName("Happy Path: Should create author with image")
        void shouldCreateAuthorWithImage() {
            // Arrange
            AuthorCreateDto createDto = new AuthorCreateDto("Tolkien", "Bio");
            MockMultipartFile imageFile = new MockMultipartFile("img", "img.png", "image/png", new byte[]{1, 2, 3});
            UploadResult uploadResult = new UploadResult("https://url", "publicId");
            Author newAuthor = new Author();
            Author savedAuthor = createMockAuthor(UUID.randomUUID(), "Tolkien");
            savedAuthor.setImage(new Image("https://url", "publicId"));

            Page<BookSummaryDto> emptyBookPage = Page.empty();
            AuthorDetailsDto expectedDto = new AuthorDetailsDto(savedAuthor.getId(), "Tolkien", "Bio", "https://url", emptyBookPage);

            given(authorRepository.findByNameIgnoreCase("Tolkien")).willReturn(Optional.empty());
            given(authorMapper.toEntity(createDto)).willReturn(newAuthor);
            given(imageUploadService.uploadImage(imageFile)).willReturn(uploadResult);
            given(authorRepository.save(any(Author.class))).willReturn(savedAuthor);
            given(authorMapper.toDetailsDto(savedAuthor, Page.empty())).willReturn(expectedDto);

            // Act
            AuthorDetailsDto result = authorService.createAuthor(createDto, imageFile);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            verify(authorRepository).save(authorCaptor.capture());
            Author capturedAuthor = authorCaptor.getValue();
            assertThat(capturedAuthor.getImage()).isNotNull();
            assertThat(capturedAuthor.getImage().getUrl()).isEqualTo("https://url");
        }

        @Test
        @DisplayName("Error Case: Should throw on duplicate name")
        void shouldThrowOnDuplicateName() {
            // Arrange
            AuthorCreateDto createDto = new AuthorCreateDto("Tolkien", "Bio");
            given(authorRepository.findByNameIgnoreCase("Tolkien")).willReturn(Optional.of(new Author()));

            // Act & Assert
            assertThatThrownBy(() -> authorService.createAuthor(createDto, null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHOR_NAME_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Happy Path: Should return author with paginated books")
        void shouldReturnAuthorWithBooks() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            Author mockAuthor = createMockAuthor(authorId, "Tolkien");
            Page<BookSummaryDto> bookPage = new PageImpl<>(Collections.emptyList());
            AuthorDetailsDto expectedDto = new AuthorDetailsDto(authorId, "Tolkien", null, null, bookPage);

            given(authorRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));
            given(bookService.findAllByAuthor(eq(authorId), any())).willReturn(bookPage);
            given(authorMapper.toDetailsDto(mockAuthor, bookPage)).willReturn(expectedDto);

            // Act
            AuthorDetailsDto result = authorService.getById(authorId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
            verify(bookService).findAllByAuthor(eq(authorId), any());
        }

        @Test
        @DisplayName("Error Case: Should throw when author not found")
        void shouldThrowWhenAuthorNotFound() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            given(authorRepository.findById(authorId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authorService.getById(authorId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateAuthor Tests")
    class UpdateAuthorTests {

        @Test
        @DisplayName("Happy Path: Should update all provided fields")
        void shouldUpdateAllFields() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            AuthorUpdateDto updateDto = AuthorUpdateDto.builder().name("New Name").summary("New Summary").build();
            Author existingAuthor = createMockAuthor(authorId, "Old Name");
            existingAuthor.setSummary("Old Summary");

            given(authorRepository.findById(authorId)).willReturn(Optional.of(existingAuthor));
            given(authorRepository.findByNameIgnoreCase("New Name")).willReturn(Optional.empty());
            given(authorRepository.save(any(Author.class))).willReturn(existingAuthor);
            given(authorMapper.toDetailsDto(any(Author.class), any())).willReturn(mock(AuthorDetailsDto.class));

            // Act
            authorService.updateAuthor(authorId, updateDto);

            // Assert
            verify(authorRepository).save(authorCaptor.capture());
            Author captured = authorCaptor.getValue();
            assertThat(captured.getName()).isEqualTo("New Name");
            assertThat(captured.getSummary()).isEqualTo("New Summary");
        }

        @Test
        @DisplayName("Edge Case: Should allow case-only name update")
        void shouldAllowCaseOnlyUpdate() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            AuthorUpdateDto updateDto = AuthorUpdateDto.builder().name("tolkien").build();
            Author existingAuthor = createMockAuthor(authorId, "Tolkien");

            given(authorRepository.findById(authorId)).willReturn(Optional.of(existingAuthor));
            given(authorRepository.findByNameIgnoreCase("tolkien")).willReturn(Optional.of(existingAuthor));
            given(authorRepository.save(any(Author.class))).willReturn(existingAuthor);
            given(authorMapper.toDetailsDto(any(Author.class), any())).willReturn(mock(AuthorDetailsDto.class));

            // Act & Assert
            assertThatCode(() -> authorService.updateAuthor(authorId, updateDto))
                    .doesNotThrowAnyException();

            verify(authorRepository).save(existingAuthor);
        }
    }

    @Nested
    @DisplayName("deleteAuthor Tests")
    class DeleteAuthorTests {
        @Test
        @DisplayName("Happy Path: Should delete author and their image")
        void shouldDeleteAuthorAndImage() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            Author mockAuthor = createMockAuthor(authorId, "Tolkien");
            mockAuthor.setImage(new Image("https://url", "publicId"));

            given(authorRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));

            // Act
            authorService.deleteAuthor(authorId);

            // Assert
            verify(authorRepository).delete(mockAuthor);
            verify(imageUploadService).deleteImage("publicId");
        }

        @Test
        @DisplayName("Error Case: Should throw when author is in use")
        void shouldThrowWhenAuthorInUse() {
            // Arrange
            UUID authorId = UUID.randomUUID();
            Author mockAuthor = createMockAuthor(authorId, "Tolkien");
            given(authorRepository.findById(authorId)).willReturn(Optional.of(mockAuthor));
            doThrow(DataIntegrityViolationException.class).when(authorRepository).delete(mockAuthor);

            // Act & Assert
            assertThatThrownBy(() -> authorService.deleteAuthor(authorId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.AUTHOR_IN_USE);

            verifyNoInteractions(imageUploadService);
        }
    }
}
