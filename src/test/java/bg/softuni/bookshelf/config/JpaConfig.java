package bg.softuni.bookshelf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.util.Optional;

/**
 * Test-specific configuration to enable JPA Auditing.
 * This is required for the @CreatedDate and @LastModifiedDate annotations
 * to work correctly in @DataJpaTest slices.
 *  Explicitly defines a DateTimeProvider to allow mocking the current time during integration tests.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(Instant.now());
    }
}