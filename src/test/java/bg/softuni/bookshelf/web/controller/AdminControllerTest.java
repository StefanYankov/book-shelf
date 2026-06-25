package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.LockUserRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockApplicationUser(roles = "ADMIN")
class AdminControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin";

    @Nested
    @DisplayName("GET /users")
    class GetAllUsersTests {
        @Test
        @DisplayName("Happy Path: Should return a paginated list of users")
        void shouldReturnPagedUsers() throws Exception {
            // Arrange
            Page<AdminUserViewDto> userPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
            given(userService.getAllUsers(any())).willReturn(userPage);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users"));

            // Assert
            result.andExpect(status().isOk())
                  .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("POST /users/{userId}/lock")
    class LockUserTests {
        @Test
        @DisplayName("Happy Path: Should call service and return 204 No Content for valid request")
        void shouldLockUserAndReturnNoContent() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Test reason");

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + userId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).lockUser(any(), any(), any());
        }

        @ParameterizedTest
        @CsvSource({
                "''",
                "'   '",
                "'\t'",
                "'\n'"
        })
        @DisplayName("Validation Error: Should return 400 Bad Request if reason is blank")
        void shouldReturn400_WhenReasonIsBlank(String blankReason) throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto(blankReason);

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + userId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest());
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("POST /users/{userId}/unlock")
    class UnlockUserTests {
        @Test
        @DisplayName("Happy Path: Should call service and return 204 No Content for valid request")
        void shouldUnlockUserAndReturnNoContent() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Test reason");

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + userId + "/unlock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).unlockUser(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Should return 403 Forbidden for non-admin user")
        void shouldReturn403_WhenUserIsNotAdmin() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/users"))
                    .andExpect(status().isForbidden());
        }
    }
}
