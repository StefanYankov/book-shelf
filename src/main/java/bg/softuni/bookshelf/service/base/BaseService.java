package bg.softuni.bookshelf.service.base;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Abstract base class providing common utility methods for domain services.
 * <p>
 * This class facilitates clean code practices by centralizing repetitive tasks
 * such as resource lookup validation and exception throwing.
 */
@Slf4j
public abstract class BaseService {

    /**
     * Executes a repository lookup and returns the entity if present, otherwise
     * throws a {@link BusinessException}.
     * <p>
     * This method implements the "lookup-or-fail" pattern by accepting a
     * {@link Supplier} to lazily execute the repository call only when needed.
     *
     * @param <T>             the type of the entity.
     * @param repositoryCall  a {@link Supplier} providing the {@link Optional}
     * result of the repository lookup.
     * @param errorCode       the {@link ErrorCode} to signal if the resource is missing.
     * @param identifier      the unique identifier used for logging/debugging purposes.
     * @return The entity if found.
     * @throws BusinessException if the repository returns an empty result.
     */
    protected <T> T findOrThrow(Supplier<Optional<T>> repositoryCall, ErrorCode errorCode, Object identifier) {
        return repositoryCall.get().orElseThrow(() -> {
            log.warn("Lookup failed. Resource [{}] not found.", identifier);
            return new BusinessException(errorCode, identifier.toString());
        });
    }
}