package bg.softuni.bookshelf.service.language;

import bg.softuni.bookshelf.data.entity.Language;
import bg.softuni.bookshelf.data.repository.LanguageRepository;
import bg.softuni.bookshelf.service.language.dto.LanguageCreateDto;
import bg.softuni.bookshelf.service.language.dto.LanguageDto;
import bg.softuni.bookshelf.service.language.dto.LanguageUpdateDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("LanguageService Unit Tests")
class LanguageServiceImplTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageServiceImpl languageService;

    @Nested
    @DisplayName("createLanguage(LanguageCreateDto) Tests")
    class CreateLanguageTests {

        @Test
        @DisplayName("Happy Path: Should create language successfully")
        void shouldCreateLanguageSuccessfully() {
            // Arrange
            LanguageCreateDto createDto = new LanguageCreateDto("English");
            Language newLanguage = new Language();
            Language savedLanguage = new Language();
            LanguageDto expectedDto = new LanguageDto(UUID.randomUUID(), "English");

            given(languageRepository.findByNameIgnoreCase("English")).willReturn(Optional.empty());
            given(languageMapper.toEntity(createDto)).willReturn(newLanguage);
            given(languageRepository.save(newLanguage)).willReturn(savedLanguage);
            given(languageMapper.toDto(savedLanguage)).willReturn(expectedDto);

            // Act
            LanguageDto result = languageService.createLanguage(createDto);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("Error Case: Should throw exception on duplicate name")
        void shouldThrowExceptionOnDuplicateName() {
            // Arrange
            LanguageCreateDto createDto = new LanguageCreateDto("English");
            given(languageRepository.findByNameIgnoreCase("English")).willReturn(Optional.of(new Language()));

            // Act & Assert
            assertThatThrownBy(() -> languageService.createLanguage(createDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LANGUAGE_NAME_DUPLICATE);

            verify(languageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read Operation Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("getById: Should return DTO when language is found")
        void getById_shouldReturnDtoWhenFound() {
            // Arrange
            UUID languageId = UUID.randomUUID();
            Language mockLanguage = new Language();
            LanguageDto expectedDto = new LanguageDto(languageId, "English");

            given(languageRepository.findById(languageId)).willReturn(Optional.of(mockLanguage));
            given(languageMapper.toDto(mockLanguage)).willReturn(expectedDto);

            // Act
            LanguageDto result = languageService.getById(languageId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("getById: Should throw exception when language is not found")
        void getById_shouldThrowExceptionWhenNotFound() {
            // Arrange
            UUID languageId = UUID.randomUUID();
            given(languageRepository.findById(languageId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> languageService.getById(languageId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LANGUAGE_NOT_FOUND);
        }

        @Test
        @DisplayName("getAll: Should return paginated DTOs")
        void getAll_shouldReturnPaginatedDtos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Language mockLanguage = new Language();
            Page<Language> languagePage = new PageImpl<>(List.of(mockLanguage), pageable, 1);
            LanguageDto dto = new LanguageDto(UUID.randomUUID(), "English");

            given(languageRepository.findAll(pageable)).willReturn(languagePage);
            given(languageMapper.toDto(mockLanguage)).willReturn(dto);

            // Act
            Page<LanguageDto> result = languageService.getAll(pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst()).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("updateLanguage(UUID, LanguageUpdateDto) Tests")
    class UpdateLanguageTests {

        @Test
        @DisplayName("Edge Case: Should allow updating name to same name with different case")
        void shouldAllowCaseOnlyUpdate() {
            // Arrange
            UUID languageId = UUID.randomUUID();
            LanguageUpdateDto updateDto = new LanguageUpdateDto("english");
            Language existingLanguage = new Language();
            existingLanguage.setId(languageId);
            existingLanguage.setName("English");

            given(languageRepository.findById(languageId)).willReturn(Optional.of(existingLanguage));
            given(languageRepository.findByNameIgnoreCase("english")).willReturn(Optional.of(existingLanguage));
            given(languageRepository.save(any(Language.class))).willReturn(existingLanguage);
            given(languageMapper.toDto(any(Language.class))).willReturn(new LanguageDto(languageId, "english"));

            // Act & Assert
            assertThatCode(() -> languageService.updateLanguage(languageId, updateDto))
                    .doesNotThrowAnyException();

            verify(languageRepository).save(existingLanguage);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if new name is taken by another language")
        void shouldThrowExceptionIfNameIsTakenByAnother() {
            // Arrange
            UUID languageToUpdateId = UUID.randomUUID();
            LanguageUpdateDto updateDto = new LanguageUpdateDto("German");

            Language languageToUpdate = new Language();
            languageToUpdate.setId(languageToUpdateId);
            languageToUpdate.setName("English");

            Language conflictingLanguage = new Language();
            conflictingLanguage.setId(UUID.randomUUID());

            given(languageRepository.findById(languageToUpdateId)).willReturn(Optional.of(languageToUpdate));
            given(languageRepository.findByNameIgnoreCase("German")).willReturn(Optional.of(conflictingLanguage));

            // Act & Assert
            assertThatThrownBy(() -> languageService.updateLanguage(languageToUpdateId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LANGUAGE_NAME_DUPLICATE);

            verify(languageRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteLanguage(UUID) Tests")
    class DeleteLanguageTests {

        @Test
        @DisplayName("Happy Path: Should delete language when found")
        void shouldDeleteLanguageWhenFound() {
            // Arrange
            UUID languageId = UUID.randomUUID();
            given(languageRepository.findById(languageId)).willReturn(Optional.of(new Language()));

            // Act
            languageService.deleteLanguage(languageId);

            // Assert
            verify(languageRepository).delete(any(Language.class));
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when language is in use")
        void shouldThrowExceptionWhenLanguageInUse() {
            // Arrange
            UUID languageId = UUID.randomUUID();
            given(languageRepository.findById(languageId)).willReturn(Optional.of(new Language()));
            doThrow(DataIntegrityViolationException.class).when(languageRepository).delete(any(Language.class));

            // Act & Assert
            assertThatThrownBy(() -> languageService.deleteLanguage(languageId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LANGUAGE_IN_USE);
        }
    }
}
