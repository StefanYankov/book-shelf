package bg.softuni.bookshelf.service.base;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Abstract base class providing common utility methods for domain services.
 * <p>
 * Centralizes repetitive tasks such as resource lookup validation and exception throwing.
 */
@Slf4j
public abstract class BaseService {

    /**
     * Executes a repository lookup and returns the entity if present, otherwise
     * throws a {@link BusinessException}.
     * <p>
     * Implements the "lookup-or-fail" pattern via a lazy {@link Supplier}.
     *
     * @param <T>            the type of the entity.
     * @param repositoryCall a {@link Supplier} providing the {@link Optional} lookup result.
     * @param errorCode      the {@link ErrorCode} to signal if the resource is missing.
     * @param identifier     the identifier used for logging/debugging (null-safe).
     * @return the entity if found.
     * @throws BusinessException if the repository returns an empty result.
     */
    protected <T> T findOrThrow(Supplier<Optional<T>> repositoryCall, ErrorCode errorCode, Object identifier) {
        return repositoryCall.get().orElseThrow(() -> {
            String id = String.valueOf(identifier);
            log.warn("Lookup failed. Resource [{}] not found.", id);
            return new BusinessException(errorCode, id);
        });
    }
}