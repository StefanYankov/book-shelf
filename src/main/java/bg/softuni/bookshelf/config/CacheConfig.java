package bg.softuni.bookshelf.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's caching abstraction with an in-memory cache manager.
 * The cache backend is provider-agnostic: switching to a distributed store (e.g. Redis)
 * is a configuration change and requires no change to the cached service methods.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache holding individual books keyed by their identifier.
     */
    public static final String BOOKS_CACHE = "books";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(BOOKS_CACHE);
    }
}