package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.TokenType;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import bg.softuni.bookshelf.service.auth.dto.ForgotPasswordRequest;
import bg.softuni.bookshelf.service.auth.dto.ResetPasswordRequest;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.shared.security.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService: Security Logic Tests")
public class AuthSecurityTests extends AbstractAuthUnitTestBase {

    @Nested
    @DisplayName("verifyEmail(String token) Tests")
    class VerifyEmailTests {

        @Test
        @DisplayName("Happy Path: Should successfully verify email, activate user, and consume token")
        void shouldVerifyEmailSuccessfully() {
            // Arrange
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ApplicationUser unverifiedUser = createMockApplicationUser();
            unverifiedUser.setActive(false);
            unverifiedUser.setEmailVerified(false);

            VerificationToken validToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.EMAIL_VERIFICATION)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .user(unverifiedUser)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(validToken));
            given(userRepository.save(any(ApplicationUser.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            authenticationService.verifyEmail(rawToken);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().isActive()).isTrue();
            assertThat(userCaptor.getValue().isEmailVerified()).isTrue();

            verify(verificationTokenRepository).delete(validToken);
        }

        @Test
        @DisplayName("Validation Error: Should throw BusinessException when token is null or blank")
        void shouldThrowWhenTokenIsBlank() {
            assertThatThrownBy(() -> authenticationService.verifyEmail("   "))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VALIDATION_FAILED);

            verifyNoInteractions(verificationTokenRepository, userRepository);
        }

        @Test
        @DisplayName("Security Error: Should throw INVALID_TOKEN when token hash not found in database")
        void shouldThrowWhenTokenNotFound() {
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.verifyEmail(rawToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_TOKEN);

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Security Error: Should throw EXPIRED_TOKEN and clean up DB when token is expired")
        void shouldThrowWhenTokenExpired() {
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ApplicationUser unverifiedUser = createMockApplicationUser();

            VerificationToken expiredToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.EMAIL_VERIFICATION)
                    .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                    .user(unverifiedUser)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authenticationService.verifyEmail(rawToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.EXPIRED_TOKEN);

            verify(verificationTokenRepository).delete(expiredToken);
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Security Error: Should throw INVALID_TOKEN when provided a PASSWORD_RESET token instead of verification")
        void shouldThrowWhenWrongTokenType() {
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ApplicationUser unverifiedUser = createMockApplicationUser();

            VerificationToken wrongTypeToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.PASSWORD_RESET)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .user(unverifiedUser)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(wrongTypeToken));

            assertThatThrownBy(() -> authenticationService.verifyEmail(rawToken))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_TOKEN);

            verify(verificationTokenRepository, never()).delete(any());
            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    @DisplayName("Password Reset Flow Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("requestPasswordReset: Happy Path - Should generate token and send email for a valid user")
        void shouldRequestPasswordResetSuccessfully() {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest("john.doe@example.com");
            ApplicationUser existingUser = createMockApplicationUser();

            given(userRepository.findByEmail("john.doe@example.com")).willReturn(Optional.of(existingUser));
            given(verificationTokenRepository.findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(any(), any())).willReturn(Optional.empty());
            given(verificationTokenRepository.save(any(VerificationToken.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            authenticationService.forgotPassword(request);

            // Assert
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getTokenType()).isEqualTo(TokenType.PASSWORD_RESET);
            assertThat(tokenCaptor.getValue().getUser()).isEqualTo(existingUser);
            assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64);
            assertThat(tokenCaptor.getValue().getExpiryDate()).isAfter(Instant.now());

            verify(emailService).sendPasswordResetEmail(eq("john.doe@example.com"), anyString());
        }

        @Test
        @DisplayName("requestPasswordReset: Security - Should silently ignore non-existent emails")
        void shouldSilentlyIgnoreNonExistentEmail() {
            // Arrange
            ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
            given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

            // Act
            authenticationService.forgotPassword(request);

            // Assert
            verifyNoInteractions(verificationTokenRepository, emailService);
        }

        @Test
        @DisplayName("resetPassword: Happy Path - Should update password and consume token")
        void shouldResetPasswordSuccessfully() {
            // Arrange
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "newStrongPassword!");
            ApplicationUser user = createMockApplicationUser();

            VerificationToken validToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.PASSWORD_RESET)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .user(user)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(validToken));
            given(passwordEncoder.encode("newStrongPassword!")).willReturn("new-hashed-password");
            given(userRepository.save(any(ApplicationUser.class))).willAnswer(invocation -> invocation.getArgument(0));

            // Act
            authenticationService.resetPassword(request);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("new-hashed-password");

            verify(verificationTokenRepository).delete(validToken);
        }

        @Test
        @DisplayName("resetPassword: Security Error - Should throw INVALID_TOKEN for wrong token type")
        void shouldThrowForWrongResetTokenType() {
            // Arrange
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "newPass");
            ApplicationUser user = createMockApplicationUser();

            VerificationToken wrongTypeToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.EMAIL_VERIFICATION)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .user(user)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(wrongTypeToken));

            // Act and Assert
            assertThatThrownBy(() -> authenticationService.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_TOKEN);

            verify(verificationTokenRepository, never()).delete(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Security Error: Should throw EXPIRED_TOKEN and clean up DB when token is expired")
        void shouldThrowWhenTokenExpired() {
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = SecurityUtils.hashSha256(rawToken);
            ResetPasswordRequest request = new ResetPasswordRequest(rawToken, "newPass");
            ApplicationUser user = createMockApplicationUser();

            VerificationToken expiredToken = VerificationToken.builder()
                    .tokenHash(hashedToken)
                    .tokenType(TokenType.PASSWORD_RESET)
                    .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                    .user(user)
                    .build();

            given(verificationTokenRepository.findByTokenHash(hashedToken)).willReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authenticationService.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.EXPIRED_TOKEN);

            verify(verificationTokenRepository).delete(expiredToken);
            verify(userRepository, never()).save(any());
        }
    }
}
