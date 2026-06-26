package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.security.auto-seed-admin", havingValue = "true")
@RequiredArgsConstructor
public class UserInitializationSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        userRepository.findByUsername("admin").ifPresent(admin -> {
            if (admin.getPassword().equals("{noop}__ADMIN_DEFAULT__")) {
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setPasswordChangeRequired(true);
                userRepository.save(admin);
            }
        });
    }
}
