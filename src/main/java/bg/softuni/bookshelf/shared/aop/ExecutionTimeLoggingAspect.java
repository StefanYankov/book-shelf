package bg.softuni.bookshelf.shared.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Logs the execution time of methods annotated with {@link LogExecutionTime}, for both normal and
 * exceptional completion.
 */
@Slf4j
@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    @Around("@annotation(bg.softuni.bookshelf.shared.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.info("{} executed in {} ms", method, millis);
            return result;
        } catch (Throwable ex) {
            long millis = (System.nanoTime() - start) / 1_000_000;
            log.info("{} failed after {} ms", method, millis);
            throw ex;
        }
    }
}