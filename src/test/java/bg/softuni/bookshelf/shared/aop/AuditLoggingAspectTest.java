package bg.softuni.bookshelf.shared.aop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuditLoggingAspect Tests")
class AuditLoggingAspectTest {

    private Sample proxied() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new Sample());
        factory.addAspect(new AuditLoggingAspect());
        return factory.getProxy();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Advises an annotated method and returns its result (authenticated principal)")
    void advisesWithPrincipal() {
        // Arrange
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "n/a", List.of()));
        UUID target = UUID.randomUUID();

        // Act
        String result = proxied().act(target);

        // Assert
        assertThat(result).isEqualTo("ok:" + target);
    }

    @Test
    @DisplayName("Advises an annotated method when unauthenticated")
    void advisesWhenAnonymous() {
        // Arrange
        UUID target = UUID.randomUUID();

        // Act
        String result = proxied().act(target);

        // Assert
        assertThat(result).isEqualTo("ok:" + target);
    }

    @Test
    @DisplayName("Propagates the exception from an annotated method")
    void propagatesException() {
        // Act & Assert
        assertThatThrownBy(() -> proxied().fail(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("denied");
    }

    static class Sample {

        @Audited
        String act(UUID targetId) {
            return "ok:" + targetId;
        }

        @Audited
        void fail(UUID targetId) {
            throw new IllegalStateException("denied");
        }
    }
}