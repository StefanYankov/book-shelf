package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.TokenType;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.data.repository.VerificationTokenRepository;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationRequest;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationResponse;
import bg.softuni.bookshelf.service.auth.dto.ForgotPasswordRequest;
import bg.softuni.bookshelf.service.auth.dto.RegisterRequest;
import bg.softuni.bookshelf.service.auth.dto.ResetPasswordRequest;
import bg.softuni.bookshelf.shared.DeveloperErrors;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.shared.infrastructure.email.EmailService;
import bg.softuni.bookshelf.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        Objects.requireNonNull(request, DeveloperErrors.DTO_NULL);
        log.debug("Attempting to register new user with username: {}", request.username());

        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            log.warn("Registration failed. Username [{}] is already taken.", normalizedUsername);
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            log.warn("Registration failed. Email [{}] is already taken.", normalizedEmail);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        ApplicationUser user = new ApplicationUser();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(hashedPassword);
        user.setActive(false);
        user.setEmailVerified(false);

        ApplicationUser savedUser = userRepository.save(user);
        log.info("User [{}] registered in pending state. ID: {}", normalizedUsername, savedUser.getId());

        verificationTokenRepository.findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(
                savedUser.getId(), TokenType.EMAIL_VERIFICATION
        ).ifPresent(verificationTokenRepository::delete);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = SecurityUtils.hashSha256(rawToken);

        VerificationToken verificationToken = VerificationToken.builder()
                .tokenHash(hashedToken)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiryDate(Instant.now().plus(Duration.ofHours(24)))
                .user(savedUser)
                .build();

        verificationTokenRepository.save(verificationToken);
        log.info("Email verification token generated for user [{}]. Token ID: {}", normalizedUsername, verificationToken.getId());

        emailService.sendVerificationEmail(savedUser.getEmail(), rawToken);
        log.info("Verification email sent to: {}", savedUser.getEmail());

        String jwtToken = jwtService.generateToken(new bg.softuni.bookshelf.config.ApplicationUserDetails(savedUser));
        return new AuthenticationResponse(jwtToken);
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // Should not happen if authenticate passes
        String jwtToken = jwtService.generateToken(new bg.softuni.bookshelf.config.ApplicationUserDetails(user));
        return new AuthenticationResponse(jwtToken);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.debug("Attempting to verify email via token.");

        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }

        String hashedToken = SecurityUtils.hashSha256(token);

        VerificationToken tokenEntity = verificationTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> {
                    log.warn("Email verification failed: Token not found in database.");
                    return new BusinessException(ErrorCode.INVALID_TOKEN);
                });

        if (tokenEntity.getTokenType() != TokenType.EMAIL_VERIFICATION) {
            log.warn("Email verification failed: Provided token is of wrong type [{}]", tokenEntity.getTokenType());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!tokenEntity.isValid()) {
            log.warn("Email verification failed: Token has expired for user [{}]", tokenEntity.getUser().getUsername());
            verificationTokenRepository.delete(tokenEntity);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = tokenEntity.getUser();
        if (user instanceof ApplicationUser appUser) {
            appUser.setEmailVerified(true);
            appUser.setActive(true);
            userRepository.save(appUser);
            log.info("Successfully verified email and activated user [{}]", user.getUsername());
        } else {
            log.warn("Email verification attempted for a non-application user [{}]. This should not happen.", user.getUsername());
        }

        verificationTokenRepository.delete(tokenEntity);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Objects.requireNonNull(request, DeveloperErrors.DTO_NULL);
        String normalizedEmail = request.email().trim().toLowerCase();
        
        log.debug("Attempting to initiate password reset for email: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        // Security: Prevent User Enumeration. Do not throw an error if the email doesn't exist.
        // We also explicitly restrict self-service resets to ApplicationUser roles only.
        if (user == null || !(user instanceof ApplicationUser)) {
            log.warn("Password reset ignored: Email [{}] not found or user is not an ApplicationUser.", normalizedEmail);
            return; 
        }

        // Clean up any existing password reset tokens for this user
        verificationTokenRepository.findFirstByUser_IdAndTokenTypeOrderByCreatedAtDesc(
                user.getId(), TokenType.PASSWORD_RESET
        ).ifPresent(verificationTokenRepository::delete);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = SecurityUtils.hashSha256(rawToken);

        VerificationToken resetToken = VerificationToken.builder()
                .tokenHash(hashedToken)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiryDate(Instant.now().plus(Duration.ofHours(1)))
                .user(user)
                .build();

        verificationTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), rawToken);

        log.info("Password reset token generated and sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.debug("Attempting to execute password reset via token.");

        Objects.requireNonNull(request, DeveloperErrors.DTO_NULL);
        
        String hashedToken = SecurityUtils.hashSha256(request.token());

        VerificationToken tokenEntity = verificationTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> {
                    log.warn("Password reset failed: Token not found in database.");
                    return new BusinessException(ErrorCode.INVALID_TOKEN);
                });

        if (tokenEntity.getTokenType() != TokenType.PASSWORD_RESET) {
            log.warn("Password reset failed: Provided token is of wrong type [{}]", tokenEntity.getTokenType());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!tokenEntity.isValid()) {
            log.warn("Password reset failed: Token has expired for user [{}]", tokenEntity.getUser().getUsername());
            verificationTokenRepository.delete(tokenEntity);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        User user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        verificationTokenRepository.delete(tokenEntity);

        log.info("Successfully reset password for user [{}]", user.getUsername());
    }
}
