package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.author.dto.AuthorDetailsDto;
import bg.softuni.bookshelf.service.author.dto.AuthorSummaryDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@DisplayName("AuthorController API Tests")
class AuthorControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/authors";

    private MockMultipartFile authorPart(String json) {
        return new MockMultipartFile("author", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
    }

    @Nested
    @DisplayName("GET /api/admin/authors")
    class ListAuthorsTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a paged list of authors")
        void adminListsAuthors() throws Exception {
            // Arrange
            AuthorSummaryDto dto = new AuthorSummaryDto(UUID.randomUUID(), "Tolkien", null);
            Page<AuthorSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
            given(authorService.getAll(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Tolkien"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(authorService).getAll(any());
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin is forbidden")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
            verifyNoInteractions(authorService);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/authors/{id}")
    class GetAuthorTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves an author by id")
        void adminGetsAuthor() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.getById(any(), any()))
                    .willReturn(new AuthorDetailsDto(id, "Tolkien", "Bio", null, Page.empty()));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Tolkien"));

            verify(authorService).getById(any(), any());
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Unknown author yields 404 via RFC 7807")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.getById(any(), any())).willThrow(new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2300"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/authors")
    class CreateAuthorTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates an author (multipart, no image) -> 201")
        void adminCreatesWithoutImage() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.createAuthor(any(), any()))
                    .willReturn(new AuthorDetailsDto(id, "Tolkien", "Bio", null, Page.empty()));

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(authorPart("{\"name\":\"Tolkien\",\"summary\":\"Bio\"}")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Tolkien"));

            verify(authorService).createAuthor(any(), any());
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates an author with an image part -> 201")
        void adminCreatesWithImage() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.createAuthor(any(), any()))
                    .willReturn(new AuthorDetailsDto(id, "Tolkien", "Bio", "https://cdn/a.jpg", Page.empty()));
            MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL)
                            .file(authorPart("{\"name\":\"Tolkien\",\"summary\":\"Bio\"}"))
                            .file(image))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.imageUrl").value("https://cdn/a.jpg"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Validation: A blank name is rejected with 400")
        void blankNameRejected() throws Exception {
            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(authorPart("{\"name\":\"\",\"summary\":\"Bio\"}")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(authorService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: A duplicate name yields 409")
        void duplicateName() throws Exception {
            // Arrange
            given(authorService.createAuthor(any(), any())).willThrow(new BusinessException(ErrorCode.AUTHOR_NAME_DUPLICATE));

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(authorPart("{\"name\":\"Tolkien\",\"summary\":\"Bio\"}")))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2302"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot create an author")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(authorPart("{\"name\":\"Tolkien\",\"summary\":\"Bio\"}")))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(authorService);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/authors/{id}")
    class UpdateAuthorTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin updates an author")
        void adminUpdates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.updateAuthor(any(), any()))
                    .willReturn(new AuthorDetailsDto(id, "New Name", "New Bio", null, Page.empty()));
            String body = "{\"name\":\"New Name\",\"summary\":\"New Bio\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Name"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Updating an unknown author yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(authorService.updateAuthor(any(), any())).willThrow(new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));
            String body = "{\"name\":\"New Name\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2300"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot update an author")
        void userForbidden() throws Exception {
            // Arrange
            String body = "{\"name\":\"New Name\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(authorService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/authors/{id}")
    class DeleteAuthorTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin deletes an author -> 204")
        void adminDeletes() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doNothing().when(authorService).deleteAuthor(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
            verify(authorService).deleteAuthor(id);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an author in use yields 409")
        void inUseConflict() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.AUTHOR_IN_USE)).when(authorService).deleteAuthor(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2301"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an unknown author yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.AUTHOR_NOT_FOUND)).when(authorService).deleteAuthor(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2300"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot delete an author")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isForbidden());
            verifyNoInteractions(authorService);
        }
    }
}