package bg.softuni.bookshelf.service.auth;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 60000L;

    @Mock
    private UserDetails userDetails;

    @Mock
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should include username and role claim when generating token with standard UserDetails")
        void shouldGenerateTokenWithRoleClaim() {
            // Arrange
            String username = "standardUser";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            when(userDetails.getUsername()).thenReturn(username);
            doReturn(authorities).when(userDetails).getAuthorities();

            // Act
            String token = jwtService.generateToken(userDetails);

            // Assert
            assertThat(token).isNotBlank();
            assertThat(jwtService.extractUsername(token)).isEqualTo(username);

            String extractedRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
            assertThat(extractedRole).isEqualTo("ROLE_USER");

            Object extractedUserId = jwtService.extractClaim(token, claims -> claims.get("userId"));
            assertThat(extractedUserId).isNull();

            verify(userDetails, times(1)).getUsername();
            verify(userDetails, times(1)).getAuthorities();
        }

        @Test
        @DisplayName("Should include custom userId and role claims when CustomUserDetails is provided")
        void shouldGenerateTokenWithCustomClaims() {
            // Arrange
            UUID userId = UUID.randomUUID();
            String username = "customUser";
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

            when(customUserDetails.getId()).thenReturn(userId);
            when(customUserDetails.getUsername()).thenReturn(username);
            doReturn(authorities).when(customUserDetails).getAuthorities();

            // Act
            String token = jwtService.generateToken(customUserDetails);

            // Assert
            assertThat(token).isNotBlank();
            assertThat(jwtService.extractUsername(token)).isEqualTo(username);

            String extractedRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
            assertThat(extractedRole).isEqualTo("ROLE_ADMIN");

            String extractedUserId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
            assertThat(extractedUserId).isEqualTo(userId.toString());

            verify(customUserDetails, times(1)).getId();
            verify(customUserDetails, times(1)).getUsername();
            verify(customUserDetails, times(1)).getAuthorities();
        }

        @Test
        @DisplayName("Should throw NullPointerException immediately when UserDetails parameter is null")
        void shouldThrowWhenUserDetailsIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> jwtService.generateToken(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Token Validation and Parsing Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return true when validating a valid non-expired token against matching UserDetails")
        void shouldValidateTokenSuccessfully() {
            // Arrange
            String username = "matchingUser";
            when(userDetails.getUsername()).thenReturn(username);
            doReturn(Collections.emptyList()).when(userDetails).getAuthorities();

            String token = jwtService.generateToken(userDetails);

            // Act
            boolean isValid = jwtService.isTokenValid(token, userDetails);

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false when validating a token against UserDetails with mismatched username")
        void shouldReturnFalseForMismatchedUsername() {
            // Arrange
            String ownerUsername = "ownerUser";
            String checkingUsername = "mismatchedUser";

            when(userDetails.getUsername()).thenReturn(ownerUsername);
            doReturn(Collections.emptyList()).when(userDetails).getAuthorities();
            String token = jwtService.generateToken(userDetails);

            UserDetails mismatchedDetails = mock(UserDetails.class);
            when(mismatchedDetails.getUsername()).thenReturn(checkingUsername);

            // Act
            boolean isValid = jwtService.isTokenValid(token, mismatchedDetails);

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should throw ExpiredJwtException when parsing expired token sequences")
        void shouldThrowWhenTokenIsExpired() {
            // Arrange
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // Set negative expiration limit
            when(userDetails.getUsername()).thenReturn("expiredUser");
            doReturn(Collections.emptyList()).when(userDetails).getAuthorities();

            String token = jwtService.generateToken(userDetails);

            // Act & Assert
            assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid.token.format",
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.wrongsignature",
                " "
        })
        @DisplayName("Should throw signature or parser exceptions when handling structurally corrupted payloads")
        void shouldThrowOnMalformedTokens(String invalidToken) {
            // Act & Assert
            assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                    .isInstanceOf(Exception.class);
        }
    }
}