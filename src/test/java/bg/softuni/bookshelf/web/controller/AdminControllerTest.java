package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.LockUserRequestDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockApplicationUser(roles = "ADMIN")
class AdminControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin";

    // --- Object Mother Factory Slices ---

    private AdminUserViewDto createMockAdminUserViewDto() {
        return AdminUserViewDto.builder()
                .id(UUID.randomUUID())
                .username("testadmin")
                .email("admin@bookshelf.bg")
                .firstName("Stefan")
                .lastName("Yankov")
                .isActive(true)
                .isEmailVerified(true)
                .role("ADMIN")
                .build();
    }

    @Nested
    @DisplayName("GET /users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Happy Path: Should return a custom paged response envelope with content mapping properties")
        void shouldReturnPagedUsersEnvelope() throws Exception {
            // Arrange
            AdminUserViewDto userDto = createMockAdminUserViewDto();
            Page<AdminUserViewDto> databasePage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 10), 1);
            given(userService.getAllUsers(any())).willReturn(databasePage);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users"));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value("testadmin"))
                    .andExpect(jsonPath("$.pageNumber").value(0))
                    .andExpect(jsonPath("$.pageSize").value(10))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.isLast").value(true));

            verify(userService).getAllUsers(any());
        }
    }

    @Nested
    @DisplayName("POST /users/{userId}/lock")
    class LockUserTests {

        @Test
        @DisplayName("Happy Path: Should call service and return 204 No Content for valid request execution")
        void shouldLockUserAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Violation of terms of service.");

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).lockUser(any(), any(), any());
        }

        @Test
        @DisplayName("Error Path: Should return 404 Not Found mapped via RFC 7807 problem detail when identifier is missing")
        void shouldReturn404_WhenTargetUserNotFound() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Spam account.");

            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).lockUser(any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + nonExistentUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:business-error"))
                    .andExpect(jsonPath("$.errorCode").value("E1005"));

            verify(userService).lockUser(any(), any(), any());
        }

        @ParameterizedTest
        @CsvSource({
                "''",
                "'   '",
                "'\t'",
                "'\n'"
        })
        @DisplayName("Validation Error: Should fail-fast and return 400 Bad Request when constraint validations reject field payload properties")
        void shouldReturn400_WhenReasonIsBlank(String blankReason) throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto(blankReason);

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:validation-error"))
                    .andExpect(jsonPath("$.errors.reason").exists());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("POST /users/{userId}/unlock")
    class UnlockUserTests {

        @Test
        @DisplayName("Happy Path: Should call service and return 204 No Content for valid request execution")
        void shouldUnlockUserAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Verification completed successfully.");

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/unlock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).unlockUser(any(), any(), any());
        }

        @Test
        @DisplayName("Error Path: Should return 404 Not Found via RFC 7807 when attempting to unlock non-existent user")
        void shouldReturn404_WhenTargetUserNotFoundOnUnlock() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Account cleared.");

            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).unlockUser(any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + nonExistentUserId + "/unlock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.errorCode").value("E1005"));

            verify(userService).unlockUser(any(), any(), any());
        }

        @ParameterizedTest
        @CsvSource({
                "''",
                "'   '",
                "'\t'",
                "'\n'"
        })
        @DisplayName("Validation Error: Should fail-fast and return 400 Bad Request when unlock reason is empty")
        void shouldReturn400_WhenUnlockReasonIsBlank(String blankReason) throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto(blankReason);

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/unlock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.errors.reason").exists());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("Security Isolation Tests")
    class SecurityIsolationTests {

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Authorization Boundary Check: Should reject request with 403 Forbidden status if missing administrative privileges")
        void shouldReturn403_WhenUserIsNotAdmin() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users"));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.title").value("Forbidden"))
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:access-denied"));

            verifyNoInteractions(userService);
        }
    }
}