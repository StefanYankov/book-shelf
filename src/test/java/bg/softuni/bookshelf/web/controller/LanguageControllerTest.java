package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.language.dto.LanguageDto;
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

@WebMvcTest(LanguageController.class)
@DisplayName("LanguageController API Tests")
class LanguageControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/languages";

    @Nested
    @DisplayName("GET /api/admin/languages")
    class ListLanguagesTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a paged list of languages")
        void adminListsLanguages() throws Exception {
            // Arrange
            LanguageDto dto = new LanguageDto(UUID.randomUUID(), "English");
            Page<LanguageDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
            given(languageService.getAll(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("English"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(languageService).getAll(any());
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin is forbidden")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
            verifyNoInteractions(languageService);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/languages/{id}")
    class GetLanguageTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin retrieves a language by id")
        void adminGetsLanguage() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(languageService.getById(id)).willReturn(new LanguageDto(id, "English"));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("English"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Unknown language yields 404 via RFC 7807")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(languageService.getById(id)).willThrow(new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2000"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/languages")
    class CreateLanguageTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates a language -> 201")
        void adminCreates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(languageService.createLanguage(any())).willReturn(new LanguageDto(id, "Bulgarian"));
            String body = objectMapper.writeValueAsString(Map.of("name", "Bulgarian"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Bulgarian"));

            verify(languageService).createLanguage(any());
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

            verifyNoInteractions(languageService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: A duplicate name yields 409")
        void duplicateName() throws Exception {
            // Arrange
            given(languageService.createLanguage(any())).willThrow(new BusinessException(ErrorCode.LANGUAGE_NAME_DUPLICATE));
            String body = objectMapper.writeValueAsString(Map.of("name", "English"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2002"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot create a language")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "Bulgarian"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(languageService);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/languages/{id}")
    class UpdateLanguageTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin updates a language")
        void adminUpdates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(languageService.updateLanguage(any(), any())).willReturn(new LanguageDto(id, "German"));
            String body = objectMapper.writeValueAsString(Map.of("name", "German"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("German"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Updating an unknown language yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(languageService.updateLanguage(any(), any())).willThrow(new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND));
            String body = objectMapper.writeValueAsString(Map.of("name", "German"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2000"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot update a language")
        void userForbidden() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("name", "German"));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(languageService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/languages/{id}")
    class DeleteLanguageTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin deletes a language -> 204")
        void adminDeletes() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doNothing().when(languageService).deleteLanguage(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
            verify(languageService).deleteLanguage(id);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting a language in use yields 409")
        void inUseConflict() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.LANGUAGE_IN_USE)).when(languageService).deleteLanguage(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E2001"));
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an unknown language yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.LANGUAGE_NOT_FOUND)).when(languageService).deleteLanguage(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2000"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot delete a language")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isForbidden());
            verifyNoInteractions(languageService);
        }
    }
}