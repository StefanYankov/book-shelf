package bg.softuni.bookshelf.service.auth;

import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.VerificationToken;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.data.repository.VerificationTokenRepository;
import bg.softuni.bookshelf.service.auth.dto.RegisterRequest;
import bg.softuni.bookshelf.shared.infrastructure.email.EmailService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public abstract class AbstractAuthUnitTestBase {

    @Mock
    protected UserRepository userRepository;
    @Mock
    protected PasswordEncoder passwordEncoder;
    @Mock
    protected JwtService jwtService;
    @Mock
    protected VerificationTokenRepository verificationTokenRepository;
    @Mock
    protected EmailService emailService;

    @InjectMocks
    protected AuthenticationServiceImpl authenticationService;

    @Captor
    protected ArgumentCaptor<ApplicationUser> userCaptor;
    @Captor
    protected ArgumentCaptor<VerificationToken> tokenCaptor;

    // --- TEST DATA FACTORY ---
    protected RegisterRequest createValidRegisterRequest() {
        return new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "johndoe",
                "password123"
        );
    }

    protected ApplicationUser createMockApplicationUser() {
        ApplicationUser user = new ApplicationUser();
        user.setId(UUID.randomUUID());
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmailVerified(false);
        return user;
    }
}
