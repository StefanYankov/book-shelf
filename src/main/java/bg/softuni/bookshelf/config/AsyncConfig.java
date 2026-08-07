package bg.softuni.bookshelf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables asynchronous method execution. Methods annotated with {@code @Async} run on a background
 * thread from Spring's task executor rather than the caller's thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

}