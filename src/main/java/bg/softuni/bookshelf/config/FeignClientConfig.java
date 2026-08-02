package bg.softuni.bookshelf.config;

import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign configuration for the reading challenge client: forwards the caller's JWT so the
 * microservice can authenticate the same user, and translates downstream HTTP errors into
 * the application's BusinessException so clients receive the standard problem response.
 */
@Slf4j
@Configuration
public class FeignClientConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Propagates the caller's Authorization header to downstream Feign requests so the
     * reading challenge microservice can validate the same JWT and resolve the user.
     */
    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader(AUTHORIZATION_HEADER);
                if (authHeader != null && !authHeader.isBlank()) {
                    template.header(AUTHORIZATION_HEADER, authHeader);
                }
            }
        };
    }

    /**
     * Maps HTTP error responses from the reading challenge microservice to domain exceptions,
     * so a downstream 404/409 surfaces as the application's own problem response.
     */
    @Bean
    public ErrorDecoder challengeErrorDecoder() {
        return (String methodKey, Response response) -> {
            HttpStatus status = HttpStatus.resolve(response.status());
            log.warn("Reading challenge service returned {} for {}", response.status(), methodKey);

            if (status == HttpStatus.NOT_FOUND) {
                return new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND);
            }
            if (status == HttpStatus.CONFLICT) {
                return new BusinessException(ErrorCode.DUPLICATE_CHALLENGE);
            }
            return new BusinessException(ErrorCode.CHALLENGE_SERVICE_UNAVAILABLE);
        };
    }
}