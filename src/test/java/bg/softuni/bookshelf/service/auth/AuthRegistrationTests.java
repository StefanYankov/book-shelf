package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.TokenType;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import bg.softuni.bookshelf.service.auth.dto.RegisterRequest;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService: Registration Logic Tests")
public class AuthRegistrationTests extends AbstractAuthUnitTestBase {

    @Nested
    @DisplayName("register(RegisterRequest) Tests")
    class RegisterTests {

        @Test
        @DisplayName("Happy Path: Should register user and send verification email")
        void shouldRegisterSuccessfully() {
            // Arrange
            RegisterRequest request = createValidRegisterRequest();
            ApplicationUser savedUser = createMockApplicationUser();

            given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());
            given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());
            given(passwordEncoder.encode(request.password())).willReturn("hashedPassword");
            given(userRepository.save(any(ApplicationUser.class))).willReturn(savedUser);

            // Act
            authenticationService.register(request);

            // Assert: User State Verification
            verify(userRepository).save(userCaptor.capture());
            ApplicationUser capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getPassword()).isEqualTo("hashedPassword");
            assertThat(capturedUser.isActive()).isTrue();
            assertThat(capturedUser.isEmailVerified()).isFalse();

            // Assert: Security Token Verification
            verify(verificationTokenRepository).save(tokenCaptor.capture());
            VerificationToken capturedToken = tokenCaptor.getValue();
            assertThat(capturedToken.getTokenType()).isEqualTo(TokenType.EMAIL_VERIFICATION);
            assertThat(capturedToken.getUser()).isEqualTo(savedUser);
            assertThat(capturedToken.getTokenHash()).hasSize(64);
            assertThat(capturedToken.getExpiryDate()).isAfter(Instant.now());

            // Assert: Infrastructure Side Effects
            verify(emailService).sendVerificationEmail(eq(request.email()), any(String.class));
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when username already exists")
        void shouldThrowWhenUsernameExists() {
            // Arrange
            RegisterRequest request = createValidRegisterRequest();
            given(userRepository.findByUsername(request.username())).willReturn(Optional.of(new ApplicationUser()));

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS);

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder, verificationTokenRepository, emailService, jwtService);
        }

        @Test
        @DisplayName("Error Case: Should throw BusinessException when email already exists")
        void shouldThrowWhenEmailExists() {
            // Arrange
            RegisterRequest request = createValidRegisterRequest();
            given(userRepository.findByUsername(request.username())).willReturn(Optional.empty());
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(new ApplicationUser()));

            // Act & Assert
            assertThatThrownBy(() -> authenticationService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Edge Case: Should normalize inputs before checking duplicates and saving")
        void shouldNormalizeInputsBeforeProcessing() {
            // Arrange
            RegisterRequest dirtyRequest = new RegisterRequest(
                    "  John  ", "  Doe  ", "  UPPER@Example.com  ", "johndoe", "password123"
            );
            ApplicationUser savedUser = createMockApplicationUser();
            savedUser.setEmail("upper@example.com");

            given(userRepository.findByUsername("johndoe")).willReturn(Optional.empty());
            given(userRepository.findByEmail("upper@example.com")).willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("hash");
            given(userRepository.save(any(ApplicationUser.class))).willReturn(savedUser);

            // Act
            authenticationService.register(dirtyRequest);

            // Assert
            verify(userRepository).findByUsername("johndoe");
            verify(userRepository).findByEmail("upper@example.com");
            verify(emailService).sendVerificationEmail(eq("upper@example.com"), any(String.class));

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getUsername()).isEqualTo("johndoe");
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("upper@example.com");
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("John");
        }
    }
}
