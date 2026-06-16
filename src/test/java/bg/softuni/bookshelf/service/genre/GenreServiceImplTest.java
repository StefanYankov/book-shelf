package bg.softuni.bookshelf.service.genre;

import bg.softuni.bookshelf.data.entity.Genre;
import bg.softuni.bookshelf.data.repository.GenreRepository;
import bg.softuni.bookshelf.service.genre.dto.GenreCreateDto;
import bg.softuni.bookshelf.service.genre.dto.GenreDto;
import bg.softuni.bookshelf.service.genre.dto.GenreUpdateDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenreService Unit Tests")
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Captor
    private ArgumentCaptor<Genre> genreCaptor;

    // --- DATA FACTORIES ---

    private GenreCreateDto createValidCreateDto(String name) {
        return new GenreCreateDto(name, "A description for " + name);
    }

    private Genre createMockGenre(UUID id, String name, String description) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        genre.setDescription(description);
        return genre;
    }

    @Nested
    @DisplayName("createGenre(GenreCreateDto) Tests")
    class CreateGenreTests {

        @Test
        @DisplayName("Happy Path: Should create genre successfully")
        void shouldCreateGenreSuccessfully() {
            // Arrange
            GenreCreateDto createDto = createValidCreateDto("Fantasy");
            Genre newGenre = new Genre();
            Genre savedGenre = createMockGenre(UUID.randomUUID(), "Fantasy", "A description");
            GenreDto expectedDto = new GenreDto(savedGenre.getId(), "Fantasy", "A description");

            given(genreRepository.findByNameIgnoreCase("Fantasy")).willReturn(Optional.empty());
            given(genreMapper.toEntity(createDto)).willReturn(newGenre);
            given(genreRepository.save(newGenre)).willReturn(savedGenre);
            given(genreMapper.toDto(savedGenre)).willReturn(expectedDto);

            // Act
            GenreDto result = genreService.createGenre(createDto);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("Error Case: Should throw exception on duplicate name")
        void shouldThrowExceptionOnDuplicateName() {
            // Arrange
            GenreCreateDto createDto = createValidCreateDto("Fantasy");
            given(genreRepository.findByNameIgnoreCase("Fantasy")).willReturn(Optional.of(new Genre()));

            // Act & Assert
            assertThatThrownBy(() -> genreService.createGenre(createDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GENRE_NAME_DUPLICATE);

            verify(genreRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read Operation Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("getById: Should return DTO when genre is found")
        void getById_shouldReturnDtoWhenFound() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            Genre mockGenre = createMockGenre(genreId, "Sci-Fi", "A description");
            GenreDto expectedDto = new GenreDto(genreId, "Sci-Fi", "A description");

            given(genreRepository.findById(genreId)).willReturn(Optional.of(mockGenre));
            given(genreMapper.toDto(mockGenre)).willReturn(expectedDto);

            // Act
            GenreDto result = genreService.getById(genreId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("getById: Should throw exception when genre is not found")
        void getById_shouldThrowExceptionWhenNotFound() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            given(genreRepository.findById(genreId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> genreService.getById(genreId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GENRE_NOT_FOUND);
        }

        @Test
        @DisplayName("getAll: Should return paginated DTOs")
        void getAll_shouldReturnPaginatedDtos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Genre mockGenre = createMockGenre(UUID.randomUUID(), "Dystopian", "A description");
            Page<Genre> genrePage = new PageImpl<>(List.of(mockGenre), pageable, 1);
            GenreDto dto = new GenreDto(mockGenre.getId(), "Dystopian", "A description");

            given(genreRepository.findAll(pageable)).willReturn(genrePage);
            given(genreMapper.toDto(mockGenre)).willReturn(dto);

            // Act
            Page<GenreDto> result = genreService.getAll(pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst()).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("updateGenre(UUID, GenreUpdateDto) Tests")
    class UpdateGenreTests {

        @Test
        @DisplayName("Happy Path: Should update only the name")
        void shouldUpdateOnlyName() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            GenreUpdateDto updateDto = new GenreUpdateDto("New Name", null);
            Genre existingGenre = createMockGenre(genreId, "Old Name", "Old Description");

            given(genreRepository.findById(genreId)).willReturn(Optional.of(existingGenre));
            given(genreRepository.findByNameIgnoreCase("New Name")).willReturn(Optional.empty());
            given(genreRepository.save(any(Genre.class))).willReturn(existingGenre);
            given(genreMapper.toDto(any(Genre.class))).willReturn(mock(GenreDto.class));

            // Act
            genreService.updateGenre(genreId, updateDto);

            // Assert
            verify(genreRepository).save(genreCaptor.capture());
            Genre captured = genreCaptor.getValue();
            assertThat(captured.getName()).isEqualTo("New Name");
            assertThat(captured.getDescription()).isEqualTo("Old Description"); // Verify other fields are untouched
        }

        @Test
        @DisplayName("Happy Path: Should update all provided fields")
        void shouldUpdateAllFields() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            GenreUpdateDto updateDto = new GenreUpdateDto("New Name", "New Description");
            Genre existingGenre = createMockGenre(genreId, "Old Name", "Old Description");

            given(genreRepository.findById(genreId)).willReturn(Optional.of(existingGenre));
            given(genreRepository.findByNameIgnoreCase("New Name")).willReturn(Optional.empty());
            given(genreRepository.save(any(Genre.class))).willReturn(existingGenre);
            given(genreMapper.toDto(any(Genre.class))).willReturn(mock(GenreDto.class));

            // Act
            genreService.updateGenre(genreId, updateDto);

            // Assert
            verify(genreRepository).save(genreCaptor.capture());
            Genre captured = genreCaptor.getValue();
            assertThat(captured.getName()).isEqualTo("New Name");
            assertThat(captured.getDescription()).isEqualTo("New Description");
        }

        @Test
        @DisplayName("Edge Case: Should allow updating name to same name with different case")
        void shouldAllowCaseOnlyUpdate() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            GenreUpdateDto updateDto = new GenreUpdateDto("fantasy", null);
            Genre existingGenre = createMockGenre(genreId, "Fantasy", "A description");

            given(genreRepository.findById(genreId)).willReturn(Optional.of(existingGenre));
            given(genreRepository.findByNameIgnoreCase("fantasy")).willReturn(Optional.of(existingGenre));
            given(genreRepository.save(any(Genre.class))).willReturn(existingGenre);
            given(genreMapper.toDto(any(Genre.class))).willReturn(new GenreDto(genreId, "fantasy", null));

            // Act & Assert
            assertThatCode(() -> genreService.updateGenre(genreId, updateDto))
                    .doesNotThrowAnyException();

            verify(genreRepository).save(existingGenre);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if new name is taken by another genre")
        void shouldThrowExceptionIfNameIsTakenByAnother() {
            // Arrange
            UUID genreToUpdateId = UUID.randomUUID();
            GenreUpdateDto updateDto = new GenreUpdateDto("Horror", null);
            Genre genreToUpdate = createMockGenre(genreToUpdateId, "Thriller", null);
            Genre conflictingGenre = createMockGenre(UUID.randomUUID(), "Horror", null);

            given(genreRepository.findById(genreToUpdateId)).willReturn(Optional.of(genreToUpdate));
            given(genreRepository.findByNameIgnoreCase("Horror")).willReturn(Optional.of(conflictingGenre));

            // Act & Assert
            assertThatThrownBy(() -> genreService.updateGenre(genreToUpdateId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GENRE_NAME_DUPLICATE);

            verify(genreRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteGenre(UUID) Tests")
    class DeleteGenreTests {

        @Test
        @DisplayName("Happy Path: Should delete genre when found")
        void shouldDeleteGenreWhenFound() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            given(genreRepository.findById(genreId)).willReturn(Optional.of(new Genre()));

            // Act
            genreService.deleteGenre(genreId);

            // Assert
            verify(genreRepository).delete(any(Genre.class));
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when genre is in use")
        void shouldThrowExceptionWhenGenreInUse() {
            // Arrange
            UUID genreId = UUID.randomUUID();
            given(genreRepository.findById(genreId)).willReturn(Optional.of(new Genre()));
            doThrow(DataIntegrityViolationException.class).when(genreRepository).delete(any(Genre.class));

            // Act & Assert
            assertThatThrownBy(() -> genreService.deleteGenre(genreId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GENRE_IN_USE);
        }
    }
}
