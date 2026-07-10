package bg.softuni.bookshelf.service.auth;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JwtService.class)
@ActiveProfiles("test")
class JwtServiceTests {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Happy Path: Should extract user details correctly from a valid token")
    void shouldExtractUserDetailsFromValidToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String role = "ROLE_USER";
        CustomUserDetails principal = new CustomUserDetails(
                userId, username, "", true, true,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );

        String token = jwtService.generateToken(principal);

        // Act
        CustomUserDetails userDetails = jwtService.extractUserDetails(token);

        // Assert
        assertThat(userDetails.getId()).isEqualTo(userId);
        assertThat(userDetails.getUsername()).isEqualTo(username);
        assertThat(userDetails.isPasswordChangeRequired()).isTrue();
        assertThat(userDetails.getAuthorities()).containsExactly(new SimpleGrantedAuthority(role));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-token",
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.fake-signature", // Invalid signature
            "eyJhbGciOiJIUzI1NiJ9.invalid-payload.signature" // Malformed payload
    })
    @DisplayName("Error Case: Should throw exception for malformed or invalid tokens")
    void shouldThrowExceptionForMalformedToken(String token) {
        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUserDetails(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Edge Case: Should throw exception for token with missing required claims")
    void shouldThrowExceptionForTokenWithMissingClaims() {
        // Arrange:
        CustomUserDetails principal = new CustomUserDetails(
                null, "testuser", "", true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtService.generateToken(principal);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractUserDetails(token))
                .isInstanceOf(Exception.class);
    }
}
