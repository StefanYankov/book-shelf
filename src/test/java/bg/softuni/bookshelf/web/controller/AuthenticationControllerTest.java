package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.dto.AuthenticationRequest;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationResponse;
import bg.softuni.bookshelf.service.auth.dto.ForgotPasswordRequest;
import bg.softuni.bookshelf.service.auth.dto.RegisterRequest;
import bg.softuni.bookshelf.service.auth.dto.ResetPasswordRequest;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/auth";

    @Nested
    @DisplayName("POST /register")
    class RegisterTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK with JWT when registration is successful")
        void shouldReturn200_WhenRegistrationIsValid() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "johndoe", "password123");
            AuthenticationResponse authResponse = new AuthenticationResponse("fake-jwt-token");
            given(authenticationService.register(any(RegisterRequest.class))).willReturn(authResponse);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"));

            verify(authenticationService).register(any(RegisterRequest.class));
        }

        @ParameterizedTest
        @CsvSource({
                ", Doe, john@doe.com, johndoe, password123", // Blank first name
                "John, , john@doe.com, johndoe, password123", // Blank last name
                "John, Doe, not-an-email, johndoe, password123", // Invalid email
                "John, Doe, john@doe.com, jd, password123", // Short username
                "John, Doe, john@doe.com, johndoe, short" // Short password
        })
        @DisplayName("Validation Error: Should return 400 Bad Request for invalid registration data")
        void shouldReturn400_WhenRegistrationIsInvalid(String firstName, String lastName, String email, String username, String password) throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(firstName, lastName, email, username, password);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            verifyNoInteractions(authenticationService);
        }

        @Test
        @DisplayName("Business Logic Error: Should return 409 Conflict when username is a duplicate")
        void shouldReturn409_WhenUsernameIsDuplicate() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "johndoe", "password123");
            given(authenticationService.register(any(RegisterRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.USERNAME_ALREADY_EXISTS.getCode()));
        }
    }

    @Nested
    @DisplayName("POST /login")
    class LoginTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK with JWT when credentials are valid")
        void shouldReturn200_WhenCredentialsAreValid() throws Exception {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("johndoe", "password123");
            AuthenticationResponse authResponse = new AuthenticationResponse("fake-jwt-token");
            given(authenticationService.authenticate(any(AuthenticationRequest.class))).willReturn(authResponse);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"));
        }

        @ParameterizedTest
        @CsvSource({
                ", password123", // Blank username
                "johndoe, "      // Blank password
        })
        @DisplayName("Validation Error: Should return 400 Bad Request for blank credentials")
        void shouldReturn400_WhenCredentialsAreBlank(String username, String password) throws Exception {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest(username, password);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            verifyNoInteractions(authenticationService);
        }

        @Test
        @DisplayName("Authentication Failure: Should return 401 Unauthorized when credentials are bad")
        void shouldReturn401_WhenCredentialsAreBad() throws Exception {
            // Arrange
            AuthenticationRequest request = new AuthenticationRequest("johndoe", "wrong-password");
            given(authenticationService.authenticate(any(AuthenticationRequest.class)))
                    .willThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid username or password."));
        }
    }

    @Nested
    @DisplayName("POST /forgot-password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK when processing a valid forgot password request")
        void shouldReturn200ForValidRequest() throws Exception {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authenticationService).forgotPassword(any(ForgotPasswordRequest.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "not-an-email"})
        @DisplayName("Validation Error: Should return 400 Bad Request for invalid email format")
        void shouldReturn400ForInvalidEmail(String invalidEmail) throws Exception {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest(invalidEmail);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(authenticationService);
        }
    }

    @Nested
    @DisplayName("POST /reset-password")
    class ResetPasswordTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK when password reset is successful")
        void shouldReturn200ForValidReset() throws Exception {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest(UUID.randomUUID().toString(), "newStrongPassword!");

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(authenticationService).resetPassword(any(ResetPasswordRequest.class));
        }

        @ParameterizedTest
        @CsvSource({
                ", newStrongPassword!", // Blank token
                "some-token, short"     // Short password
        })
        @DisplayName("Validation Error: Should return 400 Bad Request for invalid reset data")
        void shouldReturn400ForInvalidResetData(String token, String password) throws Exception {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest(token, password);

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"));

            verifyNoInteractions(authenticationService);
        }

        @Test
        @DisplayName("Security Error: Should return 401 Unauthorized if service rejects the token")
        void shouldReturn401IfTokenInvalid() throws Exception {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "newStrongPassword!");

            willThrow(new BusinessException(ErrorCode.INVALID_TOKEN))
                    .given(authenticationService).resetPassword(any(ResetPasswordRequest.class));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_TOKEN.getCode()));
        }
    }
}
