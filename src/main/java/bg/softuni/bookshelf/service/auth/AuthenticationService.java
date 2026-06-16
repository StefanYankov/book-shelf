package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.service.auth.dto.AuthenticationRequest;
import bg.softuni.bookshelf.service.auth.dto.AuthenticationResponse;
import bg.softuni.bookshelf.service.auth.dto.ForgotPasswordRequest;
import bg.softuni.bookshelf.service.auth.dto.RegisterRequest;
import bg.softuni.bookshelf.service.auth.dto.ResetPasswordRequest;

/**
 * Service interface for handling user authentication and registration.
 */
public interface AuthenticationService {

    /**
     * Registers a new user in the system.
     *
     * @param request The registration request containing user details.
     * @return An AuthenticationResponse containing a JWT if registration is successful.
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user and generates a JWT.
     *
     * @param request The authentication request containing username and password.
     * @return An AuthenticationResponse containing a JWT if authentication is successful.
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Verifies a user's email address using a secure token.
     *
     * @param token The raw verification token from the email link.
     */
    void verifyEmail(String token);

    /**
     * Initiates the password reset process for a user.
     *
     * @param request The request containing the user's email.
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets a user's password using a valid reset token.
     *
     * @param request The request containing the reset token and new password.
     */
    void resetPassword(ResetPasswordRequest request);
}
