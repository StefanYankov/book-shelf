package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.user.dto.ChangePasswordDto;
import bg.softuni.bookshelf.service.user.dto.UpdateProfileDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockApplicationUser
class UserControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/users";

    @Nested
    @DisplayName("GET /me")
    class GetMyProfileTests {
        @Test
        void shouldReturnUserProfile() throws Exception {
            // Arrange
            UserProfileDto profile = new UserProfileDto(UUID.randomUUID(), "testuser", "test@example.com", "Test", "User");
            given(userService.getProfile(any(UUID.class))).willReturn(profile);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/me"));

            // Assert
            result.andExpect(status().isOk())
                  .andExpect(jsonPath("$.username").value("testuser"));
        }
    }

    @Nested
    @DisplayName("PUT /me")
    class UpdateMyProfileTests {
        @Test
        void shouldUpdateProfileAndReturnNoContent() throws Exception {
            // Arrange
            UpdateProfileDto dto = new UpdateProfileDto("NewFirst", "NewLast");

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PUT /me/password")
    class ChangeMyPasswordTests {
        @Test
        void shouldChangePasswordAndReturnNoContent() throws Exception {
            // Arrange
            ChangePasswordDto dto = new ChangePasswordDto("currentPass", "newStrongPass");

            // Act
            ResultActions result = mockMvc.perform(put(BASE_URL + "/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result.andExpect(status().isNoContent());
        }
    }
}
