package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockApplicationUser
class UserControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/users";

    // --- Object Mother Centralized Factories ---

    private UserProfileDto createMockUserProfileDto() {
        return new UserProfileDto(
                UUID.fromString("22222222-0000-0000-0000-000000000001"),
                "testuser",
                "test@example.com",
                "John",
                "Doe"
        );
    }

    @Nested
    @DisplayName("GET /me")
    class GetMyProfileTests {

        @Test
        @DisplayName("Happy Path: Should yield user profile mapping envelope when authenticated securely")
        void shouldReturnUserProfile() throws Exception {
            // Arrange
            UserProfileDto profile = createMockUserProfileDto();
            given(userService.getProfile(any(UUID.class))).willReturn(profile);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/me"));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(profile.id().toString()))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));

            verify(userService).getProfile(any(UUID.class));
        }

        @Test
        @DisplayName("Error Path: Should return 404 Not Found via ProblemDetail if authenticated metadata record disappears")
        void shouldReturn404_WhenUserProfileMissing() throws Exception {
            // Arrange
            given(userService.getProfile(any(UUID.class)))
                    .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/me"));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.errorCode").value("E1005"));
        }
    }

    @Nested
    @DisplayName("PUT /me")
    class UpdateMyProfileTests {

        @Test
        @DisplayName("Happy Path: Should execute profile updates and return 204 No Content status context")
        void shouldUpdateProfileAndReturnNoContent() throws Exception {
            // Arrange
            UpdateProfileDto dto = new UpdateProfileDto("John", "Doe");

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
            verify(userService).updateProfile(any(UUID.class), any(UpdateProfileDto.class));
        }

        @ParameterizedTest
        @CsvSource({
                "'', 'Doe'",
                "'   ', 'Doe'",
                "'John', ''",
                "'John', '   '"
        })
        @DisplayName("Validation Boundaries: Should block invalid names layout instantly with 400 Bad Request")
        void shouldReturn400_WhenProfileFieldsViolateConstraints(String firstName, String lastName) throws Exception {
            // Arrange
            UpdateProfileDto dto = new UpdateProfileDto(firstName, lastName);

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            // Proves strict Defense in Depth contract validation shortcut
            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("PUT /me/password")
    class ChangeMyPasswordTests {

        @Test
        @DisplayName("Happy Path: Should process password changes, update claims, and return 200 OK along with fresh JWT token structure")
        void shouldChangePasswordAndReturnFreshToken() throws Exception {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("CurrentSecurePass123!", "NewExtremelyStrongPass2026!");
            given(jwtService.generateToken(any())).willReturn("eyJhbGciOiJIUzI1NiJ9.fresh_token_string");

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.fresh_token_string"));

            verify(userService).changePassword(any(UUID.class), any(ChangePasswordDto.class));
            verify(jwtService).generateToken(any());
        }

        @Test
        @DisplayName("Error Path: Should return 401 Unauthorized via ProblemDetail if current password confirmation parameters match fails")
        void shouldReturn401_WhenCurrentPasswordIsIncorrect() throws Exception {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("wrongPass", "ValidNewPass123!");
            doThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS))
                    .when(userService).changePassword(any(UUID.class), any(ChangePasswordDto.class));

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.errorCode").value("E1002"));

            verifyNoInteractions(jwtService);
        }

        @ParameterizedTest
        @CsvSource({
                "'', 'NewPass123!'",
                "'    ', 'NewPass123!'",
                "'CurrentPass123!', ''",
                "'CurrentPass123!', '    '"
        })
        @DisplayName("Validation Boundaries: Should block invalid request execution layout parameters instantly with 400 Bad Request status code")
        void shouldReturn400_WhenFieldsViolateConstraints(String currentPass, String newPass) throws Exception {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto(currentPass, newPass);

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            verifyNoInteractions(userService);
            verifyNoInteractions(jwtService);
        }
    }
}