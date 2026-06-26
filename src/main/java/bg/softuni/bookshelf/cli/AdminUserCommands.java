package bg.softuni.bookshelf.cli;

import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class AdminUserCommands {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @ShellMethod(key = "force-password-reset", value = "Forces a password reset for a user.")
    public String forcePasswordReset(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return "User not found.";
        }

        String newPassword = generateSecurePassword(12);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangeRequired(true);
        userRepository.save(user);

        return String.format("Password for user '%s' has been reset to: %s", username, newPassword);
    }

    private String generateSecurePassword(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();

        return random.ints(length, 0, allowedChars.length())
                .mapToObj(allowedChars::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
