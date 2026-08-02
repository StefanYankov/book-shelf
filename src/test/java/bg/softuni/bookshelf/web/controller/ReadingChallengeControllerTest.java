package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.challenge.dto.LogProgressDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeCreateDto;
import bg.softuni.bookshelf.service.challenge.dto.ReadingChallengeViewDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingChallengeController.class)
@DisplayName("ReadingChallengeController Web Slice Tests")
class ReadingChallengeControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/challenges";

    private final UUID challengeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private ReadingChallengeViewDto view(int booksRead, boolean completed) {
        return new ReadingChallengeViewDto(challengeId, userId, 2026, 30, booksRead, completed);
    }

    @Nested
    @DisplayName("POST /api/challenges")
    class CreateChallengeTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            // Arrange
            ReadingChallengeCreateDto dto = new ReadingChallengeCreateDto(2026, 30);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 201 Created and the challenge")
        void shouldReturn201WhenValid() throws Exception {
            // Arrange
            ReadingChallengeCreateDto dto = new ReadingChallengeCreateDto(2026, 30);
            given(readingChallengeProxyService.createChallenge(any(ReadingChallengeCreateDto.class)))
                    .willReturn(view(0, false));

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.year").value(2026))
                    .andExpect(jsonPath("$.goal").value(30))
                    .andExpect(jsonPath("$.completed").value(false));

            verify(readingChallengeProxyService).createChallenge(any(ReadingChallengeCreateDto.class));
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Validation: Should return 400 when the goal is missing")
        void shouldReturn400WhenGoalMissing() throws Exception {
            // Act & Assert: goal omitted -> @NotNull fails
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"year\":2026}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.goal").exists());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Validation: Should return 400 when the year is out of range")
        void shouldReturn400WhenYearOutOfRange() throws Exception {
            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"year\":1900,\"goal\":30}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.year").exists());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 409 when the microservice reports a duplicate")
        void shouldReturn409OnDuplicate() throws Exception {
            // Arrange
            given(readingChallengeProxyService.createChallenge(any()))
                    .willThrow(new BusinessException(ErrorCode.DUPLICATE_CHALLENGE));

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"year\":2026,\"goal\":30}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_CHALLENGE.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /api/challenges/{challengeId}/progress")
    class LogProgressTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{challengeId}/progress", challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"booksRead\":3}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 200 and the updated challenge")
        void shouldReturn200WhenValid() throws Exception {
            // Arrange
            given(readingChallengeProxyService.logProgress(eq(challengeId), any(LogProgressDto.class)))
                    .willReturn(view(10, false));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{challengeId}/progress", challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"booksRead\":3}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.booksRead").value(10));

            verify(readingChallengeProxyService).logProgress(eq(challengeId), any(LogProgressDto.class));
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Validation: Should return 400 when booksRead is missing")
        void shouldReturn400WhenBooksReadMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{challengeId}/progress", challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.booksRead").exists());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 404 when the challenge is not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            given(readingChallengeProxyService.logProgress(any(), any()))
                    .willThrow(new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{challengeId}/progress", challengeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"booksRead\":3}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.CHALLENGE_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("GET /api/challenges")
    class GetChallengeTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL).param("year", "2026"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 200 and the challenge for the year")
        void shouldReturn200WhenFound() throws Exception {
            // Arrange
            given(readingChallengeProxyService.getChallenge(2026)).willReturn(view(12, false));

            // Act & Assert
            mockMvc.perform(get(BASE_URL).param("year", "2026"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.year").value(2026))
                    .andExpect(jsonPath("$.booksRead").value(12));

            verify(readingChallengeProxyService).getChallenge(2026);
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 404 when no challenge exists for the year")
        void shouldReturn404WhenNotFound() throws Exception {
            // Arrange
            given(readingChallengeProxyService.getChallenge(2026))
                    .willThrow(new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL).param("year", "2026"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.CHALLENGE_NOT_FOUND.getCode()));
        }
    }
}