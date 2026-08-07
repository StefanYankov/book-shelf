package bg.softuni.bookshelf.shared.aop;

import bg.softuni.bookshelf.service.auth.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Audit-logs methods annotated with {@link Audited}: the acting principal, the operation, an
 * optional leading UUID target, and the outcome. Arguments are not logged, to avoid recording
 * sensitive data. Distinct from the account-status event trail, which drives account state.
 */
@Slf4j
@Aspect
@Component
public class AuditLoggingAspect {

    @Around("@annotation(bg.softuni.bookshelf.shared.aop.Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        String actor = currentPrincipal();
        String operation = joinPoint.getSignature().toShortString();
        String target = leadingUuidArgument(joinPoint.getArgs());
        long start = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.info("AUDIT: principal [{}] performed [{}]{} -> SUCCESS in {} ms",
                    actor, operation, targetSuffix(target), millis);
            return result;
        } catch (Throwable ex) {
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.warn("AUDIT: principal [{}] performed [{}]{} -> FAILURE ({}: {}) in {} ms",
                    actor, operation, targetSuffix(target), ex.getClass().getSimpleName(), ex.getMessage(), millis);
            throw ex;
        }
    }

    // Resolves a readable identity for the current principal, or "anonymous" when unauthenticated.
    private String currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }
        if (auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUsername() + " (id=" + details.getId() + ")";
        }
        return auth.getName();
    }

    // Returns the first argument if it is a UUID (a non-sensitive target identifier), else null.
    private String leadingUuidArgument(Object[] args) {
        if (args.length > 0 && args[0] instanceof UUID id) {
            return id.toString();
        }
        return null;
    }

    private String targetSuffix(String target) {
        return target == null ? "" : " on target [" + target + "]";
    }
}