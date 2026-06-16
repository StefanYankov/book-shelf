package bg.softuni.bookshelf.service.publisher;

import bg.softuni.bookshelf.data.entity.Publisher;
import bg.softuni.bookshelf.data.repository.PublisherRepository;
import bg.softuni.bookshelf.service.publisher.dto.PublisherCreateDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherDto;
import bg.softuni.bookshelf.service.publisher.dto.PublisherUpdateDto;
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
@DisplayName("PublisherService Unit Tests")
class PublisherServiceImplTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private PublisherMapper publisherMapper;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    @Captor
    private ArgumentCaptor<Publisher> publisherCaptor;

    // --- DATA FACTORIES ---

    private Publisher createMockPublisher(UUID id, String name) {
        Publisher publisher = new Publisher();
        publisher.setId(id);
        publisher.setName(name);
        return publisher;
    }

    @Nested
    @DisplayName("createPublisher(PublisherCreateDto) Tests")
    class CreatePublisherTests {

        @Test
        @DisplayName("Happy Path: Should create publisher successfully")
        void shouldCreatePublisherSuccessfully() {
            // Arrange
            PublisherCreateDto createDto = new PublisherCreateDto("Penguin Books");
            Publisher newPublisher = new Publisher();
            Publisher savedPublisher = createMockPublisher(UUID.randomUUID(), "Penguin Books");
            PublisherDto expectedDto = new PublisherDto(savedPublisher.getId(), "Penguin Books");

            given(publisherRepository.findByNameIgnoreCase("Penguin Books")).willReturn(Optional.empty());
            given(publisherMapper.toEntity(createDto)).willReturn(newPublisher);
            given(publisherRepository.save(newPublisher)).willReturn(savedPublisher);
            given(publisherMapper.toDto(savedPublisher)).willReturn(expectedDto);

            // Act
            PublisherDto result = publisherService.createPublisher(createDto);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("Error Case: Should throw exception on duplicate name")
        void shouldThrowExceptionOnDuplicateName() {
            // Arrange
            PublisherCreateDto createDto = new PublisherCreateDto("Penguin Books");
            given(publisherRepository.findByNameIgnoreCase("Penguin Books")).willReturn(Optional.of(new Publisher()));

            // Act & Assert
            assertThatThrownBy(() -> publisherService.createPublisher(createDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PUBLISHER_NAME_DUPLICATE);

            verify(publisherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read Operation Tests")
    class ReadOperationTests {

        @Test
        @DisplayName("getById: Should return DTO when publisher is found")
        void getById_shouldReturnDtoWhenFound() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            Publisher mockPublisher = createMockPublisher(publisherId, "Random House");
            PublisherDto expectedDto = new PublisherDto(publisherId, "Random House");

            given(publisherRepository.findById(publisherId)).willReturn(Optional.of(mockPublisher));
            given(publisherMapper.toDto(mockPublisher)).willReturn(expectedDto);

            // Act
            PublisherDto result = publisherService.getById(publisherId);

            // Assert
            assertThat(result).isEqualTo(expectedDto);
        }

        @Test
        @DisplayName("getById: Should throw exception when publisher is not found")
        void getById_shouldThrowExceptionWhenNotFound() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            given(publisherRepository.findById(publisherId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> publisherService.getById(publisherId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PUBLISHER_NOT_FOUND);
        }

        @Test
        @DisplayName("getAll: Should return paginated DTOs")
        void getAll_shouldReturnPaginatedDtos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Publisher mockPublisher = createMockPublisher(UUID.randomUUID(), "HarperCollins");
            Page<Publisher> publisherPage = new PageImpl<>(List.of(mockPublisher), pageable, 1);
            PublisherDto dto = new PublisherDto(mockPublisher.getId(), "HarperCollins");

            given(publisherRepository.findAll(pageable)).willReturn(publisherPage);
            given(publisherMapper.toDto(mockPublisher)).willReturn(dto);

            // Act
            Page<PublisherDto> result = publisherService.getAll(pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst()).isEqualTo(dto);
        }
    }

    @Nested
    @DisplayName("updatePublisher(UUID, PublisherUpdateDto) Tests")
    class UpdatePublisherTests {

        @Test
        @DisplayName("Happy Path: Should update name successfully")
        void shouldUpdateNameSuccessfully() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            PublisherUpdateDto updateDto = new PublisherUpdateDto("New Name");
            Publisher existingPublisher = createMockPublisher(publisherId, "Old Name");

            given(publisherRepository.findById(publisherId)).willReturn(Optional.of(existingPublisher));
            given(publisherRepository.findByNameIgnoreCase("New Name")).willReturn(Optional.empty());
            given(publisherRepository.save(any(Publisher.class))).willReturn(existingPublisher);
            given(publisherMapper.toDto(any(Publisher.class))).willReturn(mock(PublisherDto.class));

            // Act
            publisherService.updatePublisher(publisherId, updateDto);

            // Assert
            verify(publisherRepository).save(publisherCaptor.capture());
            assertThat(publisherCaptor.getValue().getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("Edge Case: Should allow updating name to same name with different case")
        void shouldAllowCaseOnlyUpdate() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            PublisherUpdateDto updateDto = new PublisherUpdateDto("penguin books");
            Publisher existingPublisher = createMockPublisher(publisherId, "Penguin Books");

            given(publisherRepository.findById(publisherId)).willReturn(Optional.of(existingPublisher));
            given(publisherRepository.findByNameIgnoreCase("penguin books")).willReturn(Optional.of(existingPublisher));
            given(publisherRepository.save(any(Publisher.class))).willReturn(existingPublisher);
            given(publisherMapper.toDto(any(Publisher.class))).willReturn(new PublisherDto(publisherId, "penguin books"));

            // Act & Assert
            assertThatCode(() -> publisherService.updatePublisher(publisherId, updateDto))
                    .doesNotThrowAnyException();

            verify(publisherRepository).save(existingPublisher);
        }

        @Test
        @DisplayName("Error Case: Should throw exception if new name is taken by another publisher")
        void shouldThrowExceptionIfNameIsTakenByAnother() {
            // Arrange
            UUID publisherToUpdateId = UUID.randomUUID();
            PublisherUpdateDto updateDto = new PublisherUpdateDto("Penguin Books");
            Publisher publisherToUpdate = createMockPublisher(publisherToUpdateId, "Old Name");
            Publisher conflictingPublisher = createMockPublisher(UUID.randomUUID(), "Penguin Books");

            given(publisherRepository.findById(publisherToUpdateId)).willReturn(Optional.of(publisherToUpdate));
            given(publisherRepository.findByNameIgnoreCase("Penguin Books")).willReturn(Optional.of(conflictingPublisher));

            // Act & Assert
            assertThatThrownBy(() -> publisherService.updatePublisher(publisherToUpdateId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PUBLISHER_NAME_DUPLICATE);

            verify(publisherRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletePublisher(UUID) Tests")
    class DeletePublisherTests {

        @Test
        @DisplayName("Happy Path: Should delete publisher when found")
        void shouldDeletePublisherWhenFound() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            given(publisherRepository.findById(publisherId)).willReturn(Optional.of(new Publisher()));

            // Act
            publisherService.deletePublisher(publisherId);

            // Assert
            verify(publisherRepository).delete(any(Publisher.class));
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when publisher is in use")
        void shouldThrowExceptionWhenPublisherInUse() {
            // Arrange
            UUID publisherId = UUID.randomUUID();
            given(publisherRepository.findById(publisherId)).willReturn(Optional.of(new Publisher()));
            doThrow(DataIntegrityViolationException.class).when(publisherRepository).delete(any(Publisher.class));

            // Act & Assert
            assertThatThrownBy(() -> publisherService.deletePublisher(publisherId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PUBLISHER_IN_USE);
        }
    }
}
