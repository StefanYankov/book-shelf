package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityFilterChainIntegrationTest.SecuredTestController.class)
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
        @DisplayName("Should allow request to proceed to admin endpoint when token claims extract ROLE_ADMIN")
        void shouldAllowAdminToAccessProtectedResource() throws Exception {
            // Arrange
            String token = "valid-admin-jwt";
            String username = "adminUser";
            CustomUserDetails principal = new CustomUserDetails(
                    UUID.randomUUID(),
                    username,
                    "password",
                    true,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            given(jwtService.extractUsername(token)).willReturn(username);
            given(jwtService.isTokenValid(eq(token), any(UserDetails.class))).willReturn(true);
            given(userDetailsService.loadUserByUsername(username)).willReturn(principal);

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isOk());
            verify(jwtService, times(1)).extractUsername(token);
            verify(jwtService, times(1)).isTokenValid(eq(token), any(UserDetails.class));
            verify(userDetailsService, times(1)).loadUserByUsername(username);
        }

        @Test
        @DisplayName("Should return 403 Forbidden on admin endpoint when token claims contain only ROLE_USER")
        void shouldBlockNormalUserFromAdminEndpoint() throws Exception {
            // Arrange
            String token = "valid-user-jwt";
            String username = "normalUser";
            CustomUserDetails principal = new CustomUserDetails(
                    UUID.randomUUID(),
                    username,
                    "password",
                    true,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );

            given(jwtService.extractUsername(token)).willReturn(username);
            given(jwtService.isTokenValid(eq(token), any(UserDetails.class))).willReturn(true);
            given(userDetailsService.loadUserByUsername(username)).willReturn(principal);

            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only")
                    .header("Authorization", "Bearer " + token));

            // Assert
            result.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized when requesting a secured endpoint without a token")
        void shouldRejectAnonymousRequestsOnProtectedResources() throws Exception {
            // Act
            ResultActions result = mockMvc.perform(get("/api/test/admin-only"));

            // Assert
            result.andExpect(status().isUnauthorized());
            verifyNoInteractions(jwtService, userDetailsService);
        }
    }
}
