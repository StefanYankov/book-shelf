package bg.softuni.bookshelf.web.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SecurityFilterChainIntegrationTest.SecuredTestController.class, UserController.class, AuthenticationController.class})
@DisplayName("Security Filter Chain Integration Tests")
class SecurityFilterChainIntegrationTest extends AbstractControllerTestBase {

    @RestController
    static class SecuredTestController {
        @GetMapping("/api/test/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        public String adminOnlyEndpoint() {
            return "admin-content";
        }

        @GetMapping("/api/test/user-only")
        @PreAuthorize("hasRole('USER')")
        public String userOnlyEndpoint() {
            return "user-content";
        }
    }

    @Nested
    @DisplayName("JWT Authentication Middleware Pipeline Tests")
    class JwtFilterTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Should allow admin user to access admin-only endpoint")
        void shouldAllowAdminToAccessProtectedResource() throws Exception {
            // Act:
            ResultActions result = mockMvc.perform(get("/api/test/admin-only"));

            // Assert
            result.andExpect(status().isOk());
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Should forbid standard user from accessing admin-only endpoint")
        void shouldForbidUserFromAdminEndpoint() throws Exception {
            // Act:
            ResultActions result = mockMvc.perform(get("/api/test/admin-only"));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @WithMockApplicationUser(roles = "USER", passwordChangeRequired = true)
        @DisplayName("Should return 403 Forbidden when password change is required")
        void shouldReturn403_WhenPasswordChangeIsRequired() throws Exception {
            // Act:
            ResultActions result = mockMvc.perform(get("/api/test/user-only"));

            // Assert
            result.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.type").value("urn:bookshelf:password-change-required"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER", passwordChangeRequired = true)
        @DisplayName("Should allow access to password change endpoint when password change is required")
        void shouldAllowAccessToPasswordChangeEndpoint() throws Exception {
            // Act:
            ResultActions result = mockMvc.perform(put("/api/users/me/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"a\",\"newPassword\":\"b\"}"));

            // Assert:
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for secured endpoint without a token")
        void shouldRejectAnonymousRequests() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only"));

            // Assert
            result.andExpect(status().isUnauthorized());
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when token is expired")
        void shouldReturn401_WhenTokenIsExpired() throws Exception {
            // Arrange
            String token = "expired-jwt";
            given(jwtService.extractUsername(token)).willThrow(new ExpiredJwtException(null, null, "Expired"));

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/user-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when token signature is invalid")
        void shouldReturn401_WhenTokenSignatureIsInvalid() throws Exception {
            // Arrange
            String token = "invalid-signature-jwt";
            given(jwtService.extractUsername(token)).willThrow(new SignatureException("Invalid signature"));

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/user-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockApplicationUser(roles = "USER", passwordChangeRequired = true)
        @DisplayName("Should allow access to public auth endpoints even when password change is required")
        void shouldAllowAccessToPublicAuthEndpoints() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/auth/non-existent-endpoint"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Regression: an EXPIRED token must NOT reject a PUBLIC route (should pass through, not 401)")
        void shouldPassThroughPublicRoute_WhenTokenIsExpired() throws Exception {
            // Arrange: an expired token, same as the protected-route case
            String token = "expired-jwt";
            given(jwtService.extractUsername(token)).willThrow(new ExpiredJwtException(null, null, "Expired"));

            // Act: hit a PUBLIC path (/api/auth/** is permitAll) with the expired token
            ResultActions result = mockMvc.perform(get("/api/auth/non-existent-endpoint")
                    .header("Authorization", "Bearer " + token));

            // Assert: reached the dispatcher (404 = no handler) rather than being rejected (401).
            // A 401 here would mean the filter hard-rejected a public route — the original bug.
            result.andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Regression: an INVALID-signature token must NOT reject a PUBLIC route")
        void shouldPassThroughPublicRoute_WhenTokenSignatureIsInvalid() throws Exception {
            // Arrange
            String token = "invalid-signature-jwt";
            given(jwtService.extractUsername(token)).willThrow(new SignatureException("Invalid signature"));

            // Act: public path with a bad token
            ResultActions result = mockMvc.perform(get("/api/auth/non-existent-endpoint")
                    .header("Authorization", "Bearer " + token));

            // Assert: passes through to 404, not 401
            result.andExpect(status().isNotFound());
        }
    }
}
