package bg.softuni.bookshelf.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduled task execution.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "application.scheduling.enabled", havingValue = "true")
public class SchedulingConfig {
}