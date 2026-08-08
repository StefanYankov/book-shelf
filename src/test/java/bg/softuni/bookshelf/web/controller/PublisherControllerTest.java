package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.publisher.dto.PublisherDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublisherController.class)
@DisplayName("PublisherController API Tests")
class PublisherControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/publishers";

    @Nested
    @DisplayName("GET /api/admin/publishers")
    class ListPublishersTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a paged list of publishers")
        void adminListsPublishers() throws Exception {
            // Arrange
            PublisherDto dto = new PublisherDto(UUID.randomUUID(), "Penguin Books");
            Page<PublisherDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
            given(publisherService.getAll(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Penguin Books"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(publisherService).getAll(any());
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin is forbidden")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
            verifyNoInteractions(publisherService);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/publishers/{id}")
    class GetPublisherTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a publisher by id")
        void adminGetsPublisher() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(publisherService.getById(id)).willReturn(new PublisherDto(id, "Random House"));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Random House"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Unknown publisher yields 404 via RFC 7807")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(publisherService.getById(id)).willThrow(new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2200"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/publishers")
    class CreatePublisherTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates a publisher -> 201")
        void adminCreates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(publisherService.createPublisher(any())).willReturn(new PublisherDto(id, "Doubleday"));
            String body = objectMapper.writeValueAsString(Map.of("name", "Doubleday"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Doubleday"));

            verify(publisherService).createPublisher(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Validation: A blank name is rejected with 400")
        void blankNameRejected(String blankName) throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", blankName));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(publisherService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: A duplicate name yields 409")
        void duplicateName() throws Exception {
            // Arrange
            given(publisherService.createPublisher(any())).willThrow(new BusinessException(ErrorCode.PUBLISHER_NAME_DUPLICATE));
            String body = objectMapper.writeValueAsString(Map.of("name", "Penguin Books"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2202"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot create a publisher")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "Doubleday"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(publisherService);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/publishers/{id}")
    class UpdatePublisherTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin updates a publisher")
        void adminUpdates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(publisherService.updatePublisher(any(), any())).willReturn(new PublisherDto(id, "New Name"));
            String body = objectMapper.writeValueAsString(Map.of("name", "New Name"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Name"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Updating an unknown publisher yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(publisherService.updatePublisher(any(), any())).willThrow(new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND));
            String body = objectMapper.writeValueAsString(Map.of("name", "New Name"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2200"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot update a publisher")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "New Name"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(publisherService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/publishers/{id}")
    class DeletePublisherTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin deletes a publisher -> 204")
        void adminDeletes() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doNothing().when(publisherService).deletePublisher(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
            verify(publisherService).deletePublisher(id);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting a publisher in use yields 409")
        void inUseConflict() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.PUBLISHER_IN_USE)).when(publisherService).deletePublisher(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2201"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an unknown publisher yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.PUBLISHER_NOT_FOUND)).when(publisherService).deletePublisher(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2200"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot delete a publisher")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isForbidden());
            verifyNoInteractions(publisherService);
        }
    }
}