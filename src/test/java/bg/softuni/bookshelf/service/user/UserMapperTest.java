package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AdminUser;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.service.user.dto.AdminUserViewDto;
import bg.softuni.bookshelf.service.user.dto.UserProfileDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    private ApplicationUser applicationUser() {
        ApplicationUser user = new ApplicationUser();
        user.setId(UUID.randomUUID());
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setEmailVerified(true);
        return user;
    }

    @Nested
    @DisplayName("toUserProfileDto Tests")
    class ToUserProfileDtoTests {

        @Test
        @DisplayName("Maps all public profile fields")
        void shouldMapProfileFields() {
            // Arrange
            ApplicationUser user = applicationUser();

            // Act
            UserProfileDto dto = userMapper.toUserProfileDto(user);

            // Assert
            assertThat(dto.id()).isEqualTo(user.getId());
            assertThat(dto.username()).isEqualTo("alice");
            assertThat(dto.email()).isEqualTo("alice@example.com");
            assertThat(dto.firstName()).isEqualTo("Alice");
            assertThat(dto.lastName()).isEqualTo("Smith");
        }
    }

    @Nested
    @DisplayName("toAdminUserViewDto Tests")
    class ToAdminUserViewDtoTests {

        @Test
        @DisplayName("An application user with no status events maps as active")
        void applicationUserWithNoEvents_isActive() {
            // Arrange
            ApplicationUser user = applicationUser();

            // Act
            AdminUserViewDto dto = userMapper.toAdminUserViewDto(user);

            // Assert
            assertThat(dto.isActive()).isTrue();
            assertThat(dto.isEmailVerified()).isTrue();
            assertThat(dto.role()).isEqualTo("ROLE_USER");
            assertThat(dto.username()).isEqualTo("alice");
            assertThat(dto.email()).isEqualTo("alice@example.com");
        }

        @Test
        @DisplayName("An admin user maps as active, verified, and ROLE_ADMIN")
        void adminUser_mapsAsAdmin() {
            // Arrange
            AdminUser admin = new AdminUser();
            admin.setId(UUID.randomUUID());
            admin.setUsername("root");
            admin.setEmail("root@example.com");
            admin.setFirstName("Root");
            admin.setLastName("Admin");

            // Act
            AdminUserViewDto dto = userMapper.toAdminUserViewDto(admin);

            // Assert
            assertThat(dto.isActive()).isTrue();
            assertThat(dto.isEmailVerified()).isTrue();
            assertThat(dto.role()).isEqualTo("ROLE_ADMIN");
        }
    }
}