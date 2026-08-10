package bg.softuni.bookshelf.integration;

import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.entity.identity.User;
import bg.softuni.bookshelf.data.enums.Permission;
import bg.softuni.bookshelf.data.repository.ApplicationUserRepository;
import bg.softuni.bookshelf.data.repository.UserRepository;
import bg.softuni.bookshelf.service.user.UserService;
import bg.softuni.bookshelf.service.user.dto.UserPermissionsDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static java.util.EnumSet.copyOf;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
public class UserServiceImplIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private UserRepository userRepository;

    private ApplicationUser persistApplicationUser(String username, Permission... permissions) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("hashed-password");
        user.setFirstName("Test");
        user.setLastName("User");
        if (permissions.length > 0) {
            user.setPermissions(copyOf(List.of(permissions)));
        }
        return applicationUserRepository.saveAndFlush(user);
    }

    @Nested
    @DisplayName("getUserPermissions(UUID)")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("Regression: permissions are readable after the transaction closes (no LazyInitializationException)")
        void shouldReturnDetachedPermissionsReadableOutsideSession() {
            // Arrange
            ApplicationUser saved = persistApplicationUser(
                    "moderator", Permission.MODERATE_REVIEWS, Permission.MODERATE_BOOKS);

            // Act
            UserPermissionsDto result = userService.getUserPermissions(saved.getId());

            // Assert
            assertThatCode(() -> result.permissions().forEach(p -> {
            }))
                    .doesNotThrowAnyException();
            assertThat(result.userId()).isEqualTo(saved.getId());
            assertThat(result.permissions())
                    .containsExactlyInAnyOrder(Permission.MODERATE_REVIEWS, Permission.MODERATE_BOOKS);
        }

        @Test
        @DisplayName("Edge Case: a user with no permissions yields an empty, safely iterable set")
        void shouldReturnEmptySetWhenNoPermissionsGranted() {
            // Arrange
            ApplicationUser saved = persistApplicationUser("plainuser");

            // Act
            UserPermissionsDto result = userService.getUserPermissions(saved.getId());

            // Assert
            assertThat(result.userId()).isEqualTo(saved.getId());
            assertThat(result.permissions()).isEmpty();
        }

        @Test
        @DisplayName("Error Case: unknown id maps to USER_NOT_FOUND")
        void shouldThrowWhenUserNotFound() {
            // Act & Assert
            assertThatThrownBy(() -> userService.getUserPermissions(UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("Error Case: an admin target is rejected as a non-standard account")
        void shouldThrowWhenTargetIsNotApplicationUser() {
            // Arrange
            AdminUser admin = new AdminUser();
            admin.setUsername("root-admin");
            admin.setEmail("root-admin@example.com");
            admin.setPassword("hashed-password");
            admin.setFirstName("Root");
            admin.setLastName("Admin");
            User savedAdmin = userRepository.saveAndFlush(admin);

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserPermissions(savedAdmin.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.PERMISSION_TARGET_INVALID);
        }
    }
}
