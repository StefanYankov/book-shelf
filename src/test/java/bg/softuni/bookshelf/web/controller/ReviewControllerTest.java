package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.review.ReviewService;
import bg.softuni.bookshelf.service.review.dto.ReviewCreateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewUpdateDto;
import bg.softuni.bookshelf.service.review.dto.ReviewViewDto;
import bg.softuni.bookshelf.shared.ValidationConstants;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController Web Slice Tests")
class ReviewControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/reviews";

    @MockitoBean
    protected ReviewService reviewService;

    // --- Object Mother Centralized Factories ---

    private ReviewViewDto createMockReviewViewDto(UUID id, UUID userId, UUID targetId, String targetType) {
        return new ReviewViewDto(
                id,
                "Excellent work",
                "Highly detailed and accurate.",
                5,
                userId,
                "testuser",
                targetId,
                targetType,
                Instant.parse("2026-07-13T10:00:00Z"),
                Instant.parse("2026-07-13T10:00:00Z")
        );
    }

    @Nested
    @DisplayName("GET /api/reviews")
    class GetReviewsForTargetTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK and PagedResponse format payload")
        void shouldReturn200AndPagedResponse() throws Exception {
            // Arrange
            UUID targetId = UUID.randomUUID();
            String targetType = "BOOK";
            ReviewViewDto mockDto = createMockReviewViewDto(UUID.randomUUID(), UUID.randomUUID(), targetId, targetType);

            Page<ReviewViewDto> page = new PageImpl<>(List.of(mockDto));
            given(reviewService.getReviewsForTarget(eq(targetId), eq(targetType), any())).willReturn(page);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL)
                    .param("targetId", targetId.toString())
                    .param("targetType", targetType));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(mockDto.id().toString()))
                    .andExpect(jsonPath("$.content[0].title").value(mockDto.title()))
                    .andExpect(jsonPath("$.content[0].targetId").value(targetId.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(reviewService).getReviewsForTarget(eq(targetId), eq(targetType), any());
        }
    }

    @Nested
    @DisplayName("POST /api/reviews")
    class AddReviewTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            // Arrange
            ReviewCreateDto dto = new ReviewCreateDto("Title", "Comment", 5);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .param("targetId", UUID.randomUUID().toString())
                            .param("targetType", "BOOK")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 201 Created and body when authenticated and valid")
        void shouldReturn201WhenAuthenticatedAndValid() throws Exception {
            // Arrange
            UUID targetId = UUID.randomUUID();
            String targetType = "BOOK";
            ReviewCreateDto dto = new ReviewCreateDto("Great Book", "Highly recommended", 5);
            ReviewViewDto mockResponse = createMockReviewViewDto(UUID.randomUUID(), UUID.randomUUID(), targetId, targetType);

            given(reviewService.addReview(any(ReviewCreateDto.class), eq(targetId), eq(targetType), any(UUID.class)))
                    .willReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .param("targetId", targetId.toString())
                            .param("targetType", targetType)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value(mockResponse.title()));

            verify(reviewService).addReview(any(ReviewCreateDto.class), eq(targetId), eq(targetType), any(UUID.class));
        }

        @ParameterizedTest(name = "Rating {0} should return 400 Bad Request")
        @ValueSource(ints = {0, 6, -1})
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Validation Boundary: Rating bounds execution")
        void shouldReturn400WhenRatingIsOutOfBounds(int invalidRating) throws Exception {
            // Arrange
            ReviewCreateDto dto = new ReviewCreateDto("Valid Title", "Valid Comment", invalidRating);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .param("targetId", UUID.randomUUID().toString())
                            .param("targetType", "BOOK")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.rating").exists());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Validation Boundary: Title maximum length exceeded")
        void shouldReturn400WhenTitleExceedsMaxLength() throws Exception {
            // Arrange
            String longTitle = "A".repeat(ValidationConstants.Review.MAX_TITLE_LENGTH + 1);
            ReviewCreateDto dto = new ReviewCreateDto(longTitle, "Valid Comment", 5);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .param("targetId", UUID.randomUUID().toString())
                            .param("targetType", "BOOK")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 409 Conflict on duplicate submission")
        void shouldReturn409OnDuplicateReview() throws Exception {
            // Arrange
            UUID targetId = UUID.randomUUID();
            ReviewCreateDto dto = new ReviewCreateDto("Great", "Comment", 5);

            given(reviewService.addReview(any(), any(), any(), any()))
                    .willThrow(new BusinessException(ErrorCode.DUPLICATE_REVIEW));

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .param("targetId", targetId.toString())
                            .param("targetType", "BOOK")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.DUPLICATE_REVIEW.getCode()));
        }
    }

    @Nested
    @DisplayName("PUT /api/reviews/{reviewId}")
    class UpdateReviewTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            // Arrange
            ReviewUpdateDto dto = new ReviewUpdateDto("Title", "Comment", 5);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{reviewId}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 200 OK when payload valid and ownership proven")
        void shouldReturn200OnValidUpdate() throws Exception {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateDto dto = new ReviewUpdateDto("Updated Title", "Updated Comment", 4);
            ReviewViewDto mockResponse = createMockReviewViewDto(reviewId, UUID.randomUUID(), UUID.randomUUID(), "BOOK");

            given(reviewService.updateReview(eq(reviewId), any(ReviewUpdateDto.class), any(UUID.class)))
                    .willReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            verify(reviewService).updateReview(eq(reviewId), any(ReviewUpdateDto.class), any(UUID.class));
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 403 Forbidden on cross-user modification attempt")
        void shouldReturn403OnUnauthorizedModification() throws Exception {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateDto dto = new ReviewUpdateDto("Hacked Title", "Hacked", 1);

            given(reviewService.updateReview(any(), any(), any()))
                    .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{reviewId}", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION.getCode()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/reviews/{reviewId}")
    class DeleteReviewTests {

        @Test
        @DisplayName("Security: Should return 401 Unauthorized when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{reviewId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Happy Path: Should return 204 and pass isAdmin=false for author deletion")
        void shouldReturn204ForAuthor() throws Exception {
            // Arrange
            UUID reviewId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{reviewId}", reviewId))
                    .andExpect(status().isNoContent());

            // A USER principal must be translated to isAdmin=false by the controller.
            verify(reviewService).deleteReview(eq(reviewId), any(UUID.class), eq(false));
        }

        @Test
        @WithMockApplicationUser(roles = {"ADMIN"})
        @DisplayName("Happy Path: Should return 204 and pass isAdmin=true for administrative override")
        void shouldReturn204ForAdminOverride() throws Exception {
            // Arrange
            UUID reviewId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{reviewId}", reviewId))
                    .andExpect(status().isNoContent());

            // An ADMIN principal must be translated to isAdmin=true — this is the whole
            // point of the boundary translation, so we assert the boolean explicitly.
            verify(reviewService).deleteReview(eq(reviewId), any(UUID.class), eq(true));
        }

        @Test
        @WithMockApplicationUser(roles = {"USER"})
        @DisplayName("Error Case: Should return 403 Forbidden when standard user attempts to delete foreign review")
        void shouldReturn403WhenDeletingForeignReview() throws Exception {
            // Arrange
            UUID reviewId = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION))
                    .when(reviewService).deleteReview(any(UUID.class), any(UUID.class), eq(false));

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{reviewId}", reviewId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED_REVIEW_MODIFICATION.getCode()));
        }
    }
}