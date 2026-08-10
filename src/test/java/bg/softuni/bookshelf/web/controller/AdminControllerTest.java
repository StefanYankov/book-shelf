package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.Permission;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.LockUserRequestDto;
import bg.softuni.bookshelf.service.user.dto.PermissionRequestDto;
import bg.softuni.bookshelf.service.user.dto.UserPermissionsDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
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

        @Test
        @DisplayName("Error Path: invalid sort property returns 400 ProblemDetail")
        void shouldReturn400_WhenSortPropertyInvalid() throws Exception {
            // Arrange
            given(userService.getAllUsers(any()))
                    .willThrow(new PropertyReferenceException(
                            "doesNotExist",
                            TypeInformation.of(
                                    User.class),
                            List.of()));

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users").param("sort", "doesNotExist"));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Invalid Sort Parameter"))
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:invalid-sort"))
                    .andExpect(jsonPath("$.detail").value("Invalid sort property: doesNotExist"));
        }
    }

    @Nested
    @DisplayName("POST /users/{userId}/lock")
    class LockUserTests {

        @Test
        @DisplayName("Happy Path: permanent lock (no duration) returns 204 and passes a null duration")
        void shouldLockUserPermanentlyAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Violation of terms of service.", null);

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).lockUser(any(), any(), any(), isNull());
        }

        @Test
        @DisplayName("Happy Path: temporary lock (duration in hours) returns 204 and passes a non-null duration")
        void shouldLockUserTemporarilyAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Cooling-off period.", 24);

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).lockUser(any(), any(), any(), notNull());
        }

        @Test
        @DisplayName("Error Path: Should return 404 Not Found mapped via RFC 7807 problem detail when identifier is missing")
        void shouldReturn404_WhenTargetUserNotFound() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto("Spam account.", null);

            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).lockUser(any(), any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + nonExistentUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:business-error"))
                    .andExpect(jsonPath("$.errorCode").value("E1005"));

            verify(userService).lockUser(any(), any(), any(), any());
        }

        @ParameterizedTest
        @CsvSource({
                "''",
                "'   '",
                "'\t'",
                "'\n'"
        })
        @DisplayName("Validation Error: Should fail-fast and return 400 Bad Request when reason is blank")
        void shouldReturn400_WhenReasonIsBlank(String blankReason) throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            LockUserRequestDto dto = new LockUserRequestDto(blankReason, null);

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

        @Test
        @DisplayName("Validation Error: Should return 400 when the lock duration is not positive")
        void shouldReturn400_WhenDurationNotPositive() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            String body = "{\"reason\":\"Temp\",\"lockDurationHours\":0}";

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/lock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.lockDurationHours").exists());

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
            LockUserRequestDto dto = new LockUserRequestDto("Verification completed successfully.", null);

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
            LockUserRequestDto dto = new LockUserRequestDto("Account cleared.", null);

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
            LockUserRequestDto dto = new LockUserRequestDto(blankReason, null);

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
    @DisplayName("POST /users/{userId}/permissions")
    class GrantPermissionTests {

        @Test
        @DisplayName("Happy Path: Should grant permission and return 204 No Content")
        void shouldGrantPermissionAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            PermissionRequestDto dto = new PermissionRequestDto(Permission.MODERATE_REVIEWS, "Trusted contributor");

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).grantPermission(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Error Path: Should return 400 when the target is not a standard user account")
        void shouldReturn400_WhenTargetInvalid() throws Exception {
            // Arrange
            UUID adminTargetId = UUID.randomUUID();
            PermissionRequestDto dto = new PermissionRequestDto(Permission.MODERATE_REVIEWS, "reason");

            doThrow(new BusinessException(ErrorCode.PERMISSION_TARGET_INVALID))
                    .when(userService).grantPermission(any(), any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + adminTargetId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"));

            verify(userService).grantPermission(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Error Path: Should return 404 when the target user does not exist")
        void shouldReturn404_WhenUserNotFound() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            PermissionRequestDto dto = new PermissionRequestDto(Permission.MODERATE_REVIEWS, "reason");

            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).grantPermission(any(), any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + nonExistentUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1005"));

            verify(userService).grantPermission(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Validation Error: Should return 400 when reason is blank")
        void shouldReturn400_WhenReasonIsBlank() throws Exception {
            // Arrange: valid permission, blank reason
            UUID targetUserId = UUID.randomUUID();
            String body = "{\"permission\":\"MODERATE_REVIEWS\",\"reason\":\"\"}";

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.reason").exists());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("Validation Error: Should return 400 when permission is an unknown value")
        void shouldReturn400_WhenPermissionInvalid() throws Exception {
            // Arrange: an enum value that doesn't exist -> deserialization fails -> 400
            UUID targetUserId = UUID.randomUUID();
            String body = "{\"permission\":\"NOT_A_REAL_PERMISSION\",\"reason\":\"reason\"}";

            // Act
            ResultActions result = mockMvc.perform(post(BASE_URL + "/users/" + targetUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            // Assert
            result.andExpect(status().isBadRequest());
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("DELETE /users/{userId}/permissions")
    class RevokePermissionTests {

        @Test
        @DisplayName("Happy Path: Should revoke permission and return 204 No Content")
        void shouldRevokePermissionAndReturnNoContent() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            PermissionRequestDto dto = new PermissionRequestDto(Permission.MODERATE_REVIEWS, "No longer needed");

            // Act
            ResultActions result = mockMvc.perform(delete(BASE_URL + "/users/" + targetUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).revokePermission(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Error Path: Should return 404 when the target user does not exist")
        void shouldReturn404_WhenUserNotFoundOnRevoke() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            PermissionRequestDto dto = new PermissionRequestDto(Permission.MODERATE_REVIEWS, "reason");

            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).revokePermission(any(), any(), any(), any());

            // Act
            ResultActions result = mockMvc.perform(delete(BASE_URL + "/users/" + nonExistentUserId + "/permissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1005"));

            verify(userService).revokePermission(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /users/{userId}/permissions")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("Happy Path: Should return 200 and the user's permissions")
        void shouldReturnPermissions() throws Exception {
            // Arrange
            UUID targetUserId = UUID.randomUUID();
            UserPermissionsDto dto = new UserPermissionsDto(targetUserId, java.util.Set.of(Permission.MODERATE_REVIEWS));
            given(userService.getUserPermissions(targetUserId)).willReturn(dto);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users/" + targetUserId + "/permissions"));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(targetUserId.toString()))
                    .andExpect(jsonPath("$.permissions[0]").value("MODERATE_REVIEWS"));

            verify(userService).getUserPermissions(targetUserId);
        }

        @Test
        @DisplayName("Error Path: Should return 404 when the target user does not exist")
        void shouldReturn404_WhenUserNotFound() throws Exception {
            // Arrange
            UUID nonExistentUserId = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.USER_NOT_FOUND))
                    .when(userService).getUserPermissions(nonExistentUserId);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/users/" + nonExistentUserId + "/permissions"));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1005"));
        }
    }
}