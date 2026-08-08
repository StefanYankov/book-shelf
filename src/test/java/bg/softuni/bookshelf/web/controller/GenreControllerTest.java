package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.genre.dto.GenreDto;
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

@WebMvcTest(GenreController.class)
@DisplayName("GenreController API Tests")
class GenreControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/genres";

    @Nested
    @DisplayName("GET /api/admin/genres")
    class ListGenresTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a paged list of genres")
        void adminListsGenres() throws Exception {
            // Arrange
            GenreDto dto = new GenreDto(UUID.randomUUID(), "Fantasy", "Magic.");
            Page<GenreDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
            given(genreService.getAll(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Fantasy"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(genreService).getAll(any());
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin is forbidden")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
            verifyNoInteractions(genreService);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/genres/{id}")
    class GetGenreTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a genre by id")
        void adminGetsGenre() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(genreService.getById(id)).willReturn(new GenreDto(id, "Fantasy", "Magic."));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Fantasy"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Unknown genre yields 404 via RFC 7807")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(genreService.getById(id)).willThrow(new BusinessException(ErrorCode.GENRE_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2100"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/genres")
    class CreateGenreTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates a genre -> 201")
        void adminCreates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(genreService.createGenre(any())).willReturn(new GenreDto(id, "Fantasy", "Magic."));
            String body = objectMapper.writeValueAsString(Map.of("name", "Fantasy", "description", "Magic."));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Fantasy"));

            verify(genreService).createGenre(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Validation: A blank name is rejected with 400")
        void blankNameRejected(String blankName) throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", blankName, "description", "Magic."));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(genreService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: A duplicate name yields 409")
        void duplicateName() throws Exception {
            // Arrange
            given(genreService.createGenre(any())).willThrow(new BusinessException(ErrorCode.GENRE_NAME_DUPLICATE));
            String body = objectMapper.writeValueAsString(Map.of("name", "Fantasy", "description", "Magic."));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2102"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot create a genre")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "Fantasy", "description", "Magic."));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(genreService);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/genres/{id}")
    class UpdateGenreTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin updates a genre")
        void adminUpdates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(genreService.updateGenre(any(), any())).willReturn(new GenreDto(id, "New", "Desc"));
            String body = objectMapper.writeValueAsString(Map.of("name", "New", "description", "Desc"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Updating an unknown genre yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(genreService.updateGenre(any(), any())).willThrow(new BusinessException(ErrorCode.GENRE_NOT_FOUND));
            String body = objectMapper.writeValueAsString(Map.of("name", "New", "description", "Desc"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2100"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot update a genre")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "New", "description", "Desc"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(genreService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/genres/{id}")
    class DeleteGenreTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin deletes a genre -> 204")
        void adminDeletes() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doNothing().when(genreService).deleteGenre(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
            verify(genreService).deleteGenre(id);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting a genre in use yields 409")
        void inUseConflict() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.GENRE_IN_USE)).when(genreService).deleteGenre(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2101"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an unknown genre yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.GENRE_NOT_FOUND)).when(genreService).deleteGenre(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2100"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot delete a genre")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isForbidden());
            verifyNoInteractions(genreService);
        }
    }
}