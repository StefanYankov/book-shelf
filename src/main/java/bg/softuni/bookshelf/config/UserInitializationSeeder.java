package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development-only seeder that assigns runtime-encoded passwords to the users inserted by the
 * reference-data migration.
 * <p>
 * The migration cannot store a usable password hash, because the correct hash depends on the
 * application's configured {@link PasswordEncoder}, which is only known at runtime. Each seeded
 * user is therefore inserted with the {@link #SEED_PASSWORD_SENTINEL} placeholder; on first boot
 * this runner replaces that placeholder with a hash produced by the live encoder, so the seeded
 * credentials always match the active encoder (eliminating pre-baked-hash mismatches).
 * <p>
 * Runs only when {@code application.security.auto-seed-admin=true} (the development profile), and is
 * idempotent: a user whose password is no longer the sentinel is left untouched, so restarts and
 * post-reset passwords are preserved.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "application.security.auto-seed-admin", havingValue = "true")
@RequiredArgsConstructor
public class UserInitializationSeeder implements CommandLineRunner {

    /**
     * Placeholder password stored by the reference-data migration for every seeded user. Only users
     * still carrying this exact value are (re)encoded here; any other value is treated as already set.
     */
    private static final String SEED_PASSWORD_SENTINEL = "{noop}__SEED_DEFAULT__";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Users with known demo credentials.
        seedKnownPassword("admin", "admin", true);      // forced rotation on first login
        seedKnownPassword("user1", "password", false);
        seedKnownPassword("user2", "password", false);
        seedKnownPassword("user3", "password", false);  // holds MODERATE_REVIEWS (seeded in migration)
        seedKnownPassword("user4", "password", false);  // holds MODERATE_BOOKS (seeded in migration)

        // user5 is deliberately given an unguessable password so the password-reset flow can be
        // demonstrated: there are no known credentials, so the only way in is via forgot-password.
        seedResetOnlyPassword("user5");
    }

    /**
     * Encodes a known raw password for a seeded user, if it still holds the sentinel placeholder.
     *
     * @param username               the seeded username.
     * @param rawPassword            the plaintext to encode with the runtime encoder.
     * @param passwordChangeRequired whether the user must rotate their password on first login.
     */
    private void seedKnownPassword(String username, String rawPassword, boolean passwordChangeRequired) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (SEED_PASSWORD_SENTINEL.equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setPasswordChangeRequired(passwordChangeRequired);
                userRepository.save(user);
                log.info("Seeded runtime-encoded password for user [{}].", username);
            }
        });
    }

    /**
     * Encodes a random, unrecoverable password for a seeded user, if it still holds the sentinel
     * placeholder. The value is never logged or stored in plaintext, so the account can only be
     * accessed by resetting its password — used to exercise the password-reset flow.
     *
     * @param username the seeded username.
     */
    private void seedResetOnlyPassword(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (SEED_PASSWORD_SENTINEL.equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setPasswordChangeRequired(false);
                userRepository.save(user);
                log.info("Seeded reset-only account [{}] with an unrecoverable password.", username);
            }
        });
    }
}