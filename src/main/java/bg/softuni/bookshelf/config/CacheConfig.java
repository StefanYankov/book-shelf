package bg.softuni.bookshelf.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Enables Spring's caching abstraction. The backend is selected by {@code spring.cache.type}
 * (redis or the in-memory default), so the same build runs with or without Redis.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache holding individual books keyed by their identifier.
     */
    public static final String BOOKS_CACHE = "books";

    /**
     * Redis cache settings, applied only when {@code spring.cache.type=redis}: values are stored as
     * JSON with a bounded time-to-live, and nulls are not cached.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisCacheConfiguration redisCacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}