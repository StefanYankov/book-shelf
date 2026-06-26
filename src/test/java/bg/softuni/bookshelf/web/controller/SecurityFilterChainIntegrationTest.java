package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SecurityFilterChainIntegrationTest.SecuredTestController.class, UserController.class})
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
        @DisplayName("Should allow request to proceed when token is valid and password change is not required")
        void shouldAllowAccess_WhenTokenIsValid() throws Exception {
            // Arrange
            String token = "valid-admin-jwt";
            String username = "adminUser";
            CustomUserDetails principal = new CustomUserDetails(
                    UUID.randomUUID(), username, "password", true, false,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            given(jwtService.extractUsername(token)).willReturn(username);
            given(jwtService.isTokenValid(eq(token), any(UserDetails.class))).willReturn(true);
            given(userDetailsService.loadUserByUsername(username)).willReturn(principal);
            given(jwtService.extractClaim(eq(token), any(Function.class))).willReturn(false);

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 Forbidden when password change is required")
        void shouldReturn403_WhenPasswordChangeIsRequired() throws Exception {
            // Arrange
            String token = "valid-user-jwt";
            given(jwtService.extractClaim(eq(token), any(Function.class))).willReturn(true);

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/user-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isForbidden())
                  .andExpect(jsonPath("$.type").value("urn:bookshelf:password-change-required"));
        }

        @Test
        @DisplayName("Should allow access to password change endpoint when password change is required")
        void shouldAllowAccessToPasswordChangeEndpoint() throws Exception {
            // Arrange
            String token = "valid-user-jwt";
            String username = "testUser";
            CustomUserDetails principal = new CustomUserDetails(
                    UUID.randomUUID(), username, "password", true, true,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            given(jwtService.extractUsername(token)).willReturn(username);
            given(jwtService.isTokenValid(eq(token), any(UserDetails.class))).willReturn(true);
            given(userDetailsService.loadUserByUsername(username)).willReturn(principal);
            given(jwtService.extractClaim(eq(token), any(Function.class))).willReturn(true);

            // Act
            ResultActions result = mockMvc.perform(put("/api/users/me/password")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"currentPassword\":\"a\",\"newPassword\":\"b\"}"));

            // Assert
            result.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for secured endpoint without a token")
        void shouldRejectAnonymousRequests() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only"));

            // Assert
            result.andExpect(status().isUnauthorized());
            verifyNoInteractions(jwtService, userDetailsService);
        }
    }
}
