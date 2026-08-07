package bg.softuni.bookshelf.shared.aop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExecutionTimeLoggingAspect Tests")
class ExecutionTimeLoggingAspectTest {

    private Sample proxied() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new Sample());
        factory.addAspect(new ExecutionTimeLoggingAspect());
        return factory.getProxy();
    }

    @Test
    @DisplayName("Advises an annotated method and returns its result")
    void advisesAnnotatedMethod() {
        // Act
        String result = proxied().ok();

        // Assert
        assertThat(result).isEqualTo("done");
    }

    @Test
    @DisplayName("Propagates the exception from an annotated method")
    void propagatesException() {
        // Act & Assert
        assertThatThrownBy(() -> proxied().boom())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    @DisplayName("Leaves an unannotated method's result unchanged")
    void unannotatedPassesThrough() {
        // Act
        String result = proxied().unannotated();

        // Assert
        assertThat(result).isEqualTo("plain");
    }

    static class Sample {

        @LogExecutionTime
        String ok() {
            return "done";
        }

        @LogExecutionTime
        void boom() {
            throw new IllegalStateException("boom");
        }

        String unannotated() {
            return "plain";
        }
    }
}